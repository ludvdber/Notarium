# Deploy — Proxmox LXC

Target: one "app" LXC running the Freenote fat jar + systemd, with sibling LXCs for Postgres,
Redis, MinIO and Meilisearch.

## Build the deployable

```bash
./gradlew bootJar            # produces build/libs/freenote-0.0.1-SNAPSHOT.jar
                             # The fat jar embeds the Vite SPA under /BOOT-INF/classes/static/.
```

The jar is ~99 MB. Everything needed to run (backend + frontend) is inside. No nginx is
strictly required to serve the SPA — the jar can do it alone — but in a Proxmox setup you
will want nginx in front for TLS termination, gzip on static assets, HSTS/security headers.

## LXC layout

```
proxmox host
├── lxc-freenote-app        (this jar, systemd, ports 8080 internal)
├── lxc-postgres            (Postgres 17, port 5432)
├── lxc-redis               (Redis 7, port 6379)
├── lxc-minio               (MinIO, ports 9000 + 9001)
├── lxc-meilisearch         (Meilisearch, port 7700)
└── lxc-nginx               (nginx reverse proxy, 80/443 exposed)
```

Keep them on a private bridge (e.g. `vmbr1`) so only the nginx LXC has a routable IP.
Use `/etc/hosts` entries on `lxc-freenote-app` so the env file can reference
`pg.freenote.lan`, `redis.freenote.lan`, etc.

## First-time install on lxc-freenote-app

```bash
# Debian 12 LXC with Java 25 — adjust the repo if needed.
apt update && apt install -y openjdk-25-jre-headless curl postgresql-client age

useradd --system --home /opt/freenote --create-home --shell /usr/sbin/nologin freenote

install -d -o freenote -g freenote /opt/freenote /var/log/freenote
install -d -m 0750 -o root -g freenote /etc/freenote

# Drop the jar
scp build/libs/freenote-0.0.1-SNAPSHOT.jar root@lxc-freenote-app:/opt/freenote/freenote.jar
chown freenote:freenote /opt/freenote/freenote.jar

# Populate env file
scp deploy/freenote.env.example root@lxc-freenote-app:/etc/freenote/freenote.env
vim /etc/freenote/freenote.env          # replace every change-me
chmod 600 /etc/freenote/freenote.env
chown freenote:freenote /etc/freenote/freenote.env

# Systemd unit
scp deploy/freenote.service root@lxc-freenote-app:/etc/systemd/system/freenote.service
systemctl daemon-reload
systemctl enable --now freenote
journalctl -u freenote -f   # verify boot
```

## Updating to a new release

```bash
./gradlew bootJar
scp build/libs/freenote-0.0.1-SNAPSHOT.jar root@lxc-freenote-app:/opt/freenote/freenote.jar.new
ssh root@lxc-freenote-app '
    mv /opt/freenote/freenote.jar.new /opt/freenote/freenote.jar &&
    chown freenote:freenote /opt/freenote/freenote.jar &&
    systemctl restart freenote
'
```

Flyway applies any new migrations at startup (`ddl-auto=validate`, so the schema must match
the entities exactly — never edit a shipped migration, always add the next `V*` file).

## Backups

```bash
# On lxc-freenote-app:
scp deploy/backup.sh root@lxc-freenote-app:/usr/local/bin/freenote-backup.sh
chmod +x /usr/local/bin/freenote-backup.sh
scp deploy/freenote-backup.service deploy/freenote-backup.timer root@lxc-freenote-app:/etc/systemd/system/
systemctl daemon-reload
systemctl enable --now freenote-backup.timer
```

The script dumps Postgres + mirrors MinIO, encrypts everything with `age`, and (optionally)
pushes to Backblaze B2 via the MinIO client.

- **Never** commit the age PRIVATE key — keep it offline (paper + USB, or in a password
  manager). Rotating it just means re-encrypting future backups against a new recipient.
- Proxmox LXC snapshots complement (not replace) this: they give you fast full-container
  rollback, pg_dump gives you selective / cross-version recovery.

Restore drill (do this once a quarter):

```bash
# Assumes backups/ has been restored from offsite.
age --decrypt -i ~/backup.key -o postgres.dump postgres-20260420T031500Z.dump.age
pg_restore --clean --if-exists --no-owner --dbname=freenote_restore postgres.dump
```

## Nginx

Use `deploy/nginx.conf.example` on `lxc-nginx`. It terminates TLS with certbot, forwards
`/` to the app LXC, and adds the security headers that the Spring CSP doesn't cover
(HSTS, X-Content-Type-Options, Referrer-Policy, Permissions-Policy). It also has a dedicated
`location /api/notifications/stream` block with `proxy_buffering off` so the SSE notification
feed flushes in real time.

### Behind Cloudflare (Cloudflare → nginx → backend)

If Cloudflare sits in front, uncomment the `real_ip` block at the top of `nginx.conf.example`:
Cloudflare connects from its own IPs, so without `real_ip_header CF-Connecting-IP` +
`set_real_ip_from <Cloudflare CIDRs>`, every request appears to come from ~20 Cloudflare IPs —
the backend's per-IP rate limiting becomes useless and access logs are wrong. Keep the CIDR list
synced with <https://www.cloudflare.com/ips/>. Set Cloudflare SSL/TLS mode to **Full (strict)**
so the edge→origin hop is HTTPS too, and keep `TRUSTED_PROXIES` (backend env) pointing at the
nginx LXC IP — the backend only ever sees nginx as its immediate proxy, never Cloudflare.

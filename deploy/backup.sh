#!/usr/bin/env bash
# Daily backup of Postgres (pg_dump) + MinIO bucket (mc mirror).
# Designed for a Proxmox LXC host running freenote. Complements (not replaces) Proxmox
# LXC snapshots — pg_dump is restorable across Postgres versions and partial table recovery,
# which snapshots are not.
#
# Install: copy to /usr/local/bin/freenote-backup.sh, chmod +x, then add a systemd timer or
# a cron line: `0 3 * * * /usr/local/bin/freenote-backup.sh >> /var/log/freenote/backup.log 2>&1`
#
# Requires: pg_dump (postgresql-client), mc (MinIO client), age (or gpg) for encryption.

set -euo pipefail

# --- Config ---------------------------------------------------------------
BACKUP_ROOT="${BACKUP_ROOT:-/var/backups/freenote}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
TS="$(date -u +%Y%m%dT%H%M%SZ)"

# Postgres — read from the same env file that the app uses.
# shellcheck disable=SC1091
source /etc/freenote/freenote.env

# MinIO target for offsite backup (create with `mc alias set …`)
# e.g. `mc alias set b2 https://s3.eu-central-003.backblazeb2.com <key> <secret>`
OFFSITE_ALIAS="${OFFSITE_ALIAS:-b2/freenote-backups}"

# Age public key (recipient) used for backup encryption.
# Generate with: age-keygen -o /etc/freenote/backup.key  (keep the PRIVATE key offline!)
AGE_RECIPIENT_FILE="${AGE_RECIPIENT_FILE:-/etc/freenote/backup.pub}"

# --- Helpers --------------------------------------------------------------
log() { echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] $*"; }
die() { log "FATAL: $*" >&2; exit 1; }

command -v pg_dump >/dev/null || die "pg_dump not installed"
command -v mc      >/dev/null || die "mc (MinIO client) not installed"
command -v age     >/dev/null || die "age not installed"
[[ -r "$AGE_RECIPIENT_FILE" ]] || die "age recipient pubkey missing: $AGE_RECIPIENT_FILE"

mkdir -p "$BACKUP_ROOT"
cd "$BACKUP_ROOT"

# --- 1. Postgres dump (compressed + encrypted) ---------------------------
log "Dumping Postgres database"
# Extract host from jdbc URL: jdbc:postgresql://host:port/db
PG_HOST=$(echo "$DB_URL" | sed -E 's#jdbc:postgresql://([^:/]+).*#\1#')
PG_PORT=$(echo "$DB_URL" | sed -E 's#jdbc:postgresql://[^:]+:([0-9]+)/.*#\1#')
PG_DB=$(echo   "$DB_URL" | sed -E 's#.*/([^?]+).*#\1#')

PGPASSWORD="$DB_PASSWORD" pg_dump \
    --host="$PG_HOST" --port="$PG_PORT" --username="$DB_USERNAME" \
    --format=custom --compress=9 --no-owner --no-privileges \
    --dbname="$PG_DB" \
    | age -R "$AGE_RECIPIENT_FILE" -o "postgres-${TS}.dump.age"

log "Postgres dump OK ($(du -h "postgres-${TS}.dump.age" | cut -f1))"

# --- 2. MinIO mirror -----------------------------------------------------
log "Mirroring MinIO bucket"
# Local mirror so Proxmox LXC snapshot also captures MinIO state.
mc alias set local-minio "$MINIO_ENDPOINT" "$MINIO_ACCESS_KEY" "$MINIO_SECRET_KEY" >/dev/null
mc mirror --overwrite --remove "local-minio/${MINIO_BUCKET}" "./minio-${TS}/"
tar -c "minio-${TS}" | age -R "$AGE_RECIPIENT_FILE" -o "minio-${TS}.tar.age"
rm -rf "./minio-${TS}"
log "MinIO mirror OK ($(du -h "minio-${TS}.tar.age" | cut -f1))"

# --- 3. Push offsite (Backblaze B2 or similar) ---------------------------
if mc alias list "$OFFSITE_ALIAS" >/dev/null 2>&1; then
    log "Uploading to offsite: $OFFSITE_ALIAS"
    mc cp "postgres-${TS}.dump.age" "minio-${TS}.tar.age" "$OFFSITE_ALIAS/" >/dev/null
    log "Offsite upload OK"
else
    log "WARN: offsite alias '$OFFSITE_ALIAS' not configured — keeping local copy only"
fi

# --- 4. Retention --------------------------------------------------------
log "Pruning local backups older than ${RETENTION_DAYS} days"
find "$BACKUP_ROOT" -maxdepth 1 -type f -name '*.age' -mtime "+${RETENTION_DAYS}" -delete

log "Backup complete"

package be.freenote.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Stores revoked JWT IDs (jti) in Redis with TTL = remaining token lifetime.
 * A stolen JWT can thus be invalidated server-side before its natural expiration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtRevocationService {

    private static final String PREFIX = "jwt-revoked:";

    private final StringRedisTemplate redisTemplate;

    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) return false;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
        } catch (DataAccessException ex) {
            // Fail-open (availability > edge-case stolen-token window during Redis outage).
            // ERROR level ensures the outage is visible in monitoring/alerting.
            log.error("Redis unreachable during JWT revocation check — allowing token (jti={})", jti, ex);
            return false;
        }
    }

    public void revoke(String jti, Instant expiresAt) {
        if (jti == null || jti.isBlank() || expiresAt == null) return;
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) return;
        try {
            redisTemplate.opsForValue().set(PREFIX + jti, "1", ttl);
        } catch (DataAccessException ex) {
            // Revocation best-effort: if Redis is down, the token will still expire naturally via its exp claim.
            log.error("Redis unreachable during JWT revocation write (jti={})", jti, ex);
        }
    }
}

package be.freenote.security;

import be.freenote.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String VALID_SECRET = "a".repeat(64); // 64 bytes for HS512

    private JwtTokenProvider provider(long expirationMs) {
        return new JwtTokenProvider(VALID_SECRET, expirationMs);
    }

    // ---- Token generation & validation ----

    @Test
    void shouldGenerateValidToken() {
        JwtTokenProvider p = provider(3600000);
        User user = User.builder().id(42L).username("test").verified(true).role("ADMIN").build();

        String token = p.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(p.validateToken(token)).isTrue();
    }

    @Test
    void shouldExtractUserIdFromToken() {
        JwtTokenProvider p = provider(3600000);
        User user = User.builder().id(42L).username("test").verified(false).role("USER").build();

        String token = p.generateToken(user);

        assertThat(p.getUserIdFromToken(token)).isEqualTo(42L);
    }

    @Test
    void shouldExtractVerifiedClaimFromToken() {
        JwtTokenProvider p = provider(3600000);

        User verifiedUser = User.builder().id(1L).username("v").verified(true).role("USER").build();
        User unverifiedUser = User.builder().id(2L).username("u").verified(false).role("USER").build();

        assertThat(p.isVerified(p.generateToken(verifiedUser))).isTrue();
        assertThat(p.isVerified(p.generateToken(unverifiedUser))).isFalse();
    }

    @Test
    void shouldExtractRoleFromToken() {
        JwtTokenProvider p = provider(3600000);

        User admin = User.builder().id(1L).username("a").verified(false).role("ADMIN").build();
        User regular = User.builder().id(2L).username("r").verified(false).role("USER").build();

        assertThat(p.getRole(p.generateToken(admin))).isEqualTo("ADMIN");
        assertThat(p.getRole(p.generateToken(regular))).isEqualTo("USER");
    }

    // ---- Token rejection ----

    @Test
    void shouldRejectExpiredToken() {
        JwtTokenProvider p = provider(-5000); // expired 5s ago
        User user = User.builder().id(1L).username("t").verified(false).role("USER").build();

        String token = p.generateToken(user);

        assertThat(p.validateToken(token)).isFalse();
    }

    @Test
    void shouldRejectMalformedToken() {
        JwtTokenProvider p = provider(3600000);

        assertThat(p.validateToken("not.a.valid.jwt")).isFalse();
        assertThat(p.validateToken("random-string")).isFalse();
        assertThat(p.validateToken("")).isFalse();
    }

    @Test
    void shouldRejectTokenSignedWithDifferentKey() {
        JwtTokenProvider p1 = new JwtTokenProvider("a".repeat(64), 3600000);
        JwtTokenProvider p2 = new JwtTokenProvider("b".repeat(64), 3600000);

        User user = User.builder().id(1L).username("t").verified(false).role("USER").build();
        String token = p1.generateToken(user);

        assertThat(p2.validateToken(token)).isFalse();
    }

    // ---- Constructor validation ----

    @Test
    void shouldThrowWhenSecretTooShort() {
        assertThatThrownBy(() -> new JwtTokenProvider("short-secret", 3600000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("64 bytes");
    }

    @Test
    void shouldAcceptExactly64ByteSecret() {
        assertThatCode(() -> new JwtTokenProvider("x".repeat(64), 3600000))
                .doesNotThrowAnyException();
    }
}

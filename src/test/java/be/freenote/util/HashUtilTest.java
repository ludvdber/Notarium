package be.freenote.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class HashUtilTest {

    @Test
    void shouldProduceConsistentHash() {
        String hash1 = HashUtil.hashEmail("test@isfce.be", "salt");
        String hash2 = HashUtil.hashEmail("test@isfce.be", "salt");

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void shouldProduceDifferentHashForDifferentEmails() {
        String hash1 = HashUtil.hashEmail("alice@isfce.be", "salt");
        String hash2 = HashUtil.hashEmail("bob@isfce.be", "salt");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldProduceDifferentHashForDifferentSalts() {
        String hash1 = HashUtil.hashEmail("test@isfce.be", "salt1");
        String hash2 = HashUtil.hashEmail("test@isfce.be", "salt2");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldNormalizeToCaseInsensitive() {
        String hash1 = HashUtil.hashEmail("Test@ISFCE.BE", "salt");
        String hash2 = HashUtil.hashEmail("test@isfce.be", "salt");

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void shouldTrimWhitespace() {
        String hash1 = HashUtil.hashEmail("  test@isfce.be  ", "salt");
        String hash2 = HashUtil.hashEmail("test@isfce.be", "salt");

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void shouldReturnHexString() {
        String hash = HashUtil.hashEmail("test@isfce.be", "salt");

        assertThat(hash).matches("[0-9a-f]{64}"); // SHA-256 = 64 hex chars
    }
}

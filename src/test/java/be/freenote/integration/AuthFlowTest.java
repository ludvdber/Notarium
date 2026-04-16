package be.freenote.integration;

import be.freenote.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.mail.internet.MimeMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests the email verification flow end-to-end via MockMvc.
 * JavaMailSender is mocked — we test the Redis code storage, brute-force
 * protection, and user state transitions, not SMTP delivery.
 */
@Tag("integration")
class AuthFlowTest extends AbstractIntegrationTest {

    @MockitoBean private JavaMailSender mailSender;

    private User unverifiedUser;
    private String jwt;

    @BeforeEach
    void setUp() {
        ratingRepository.deleteAll();
        favoriteRepository.deleteAll();
        badgeRepository.deleteAll();
        donationRepository.deleteAll();
        documentRepository.deleteAll();
        courseRepository.deleteAll();
        sectionRepository.deleteAll();
        userRepository.deleteAll();
        var keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);

        unverifiedUser = createUser("auth-test-user", false, "USER");
        jwt = jwtFor(unverifiedUser);

        // Mock mail sender to avoid actual SMTP
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
    }

    @Test
    void shouldCompleteVerificationFlow() throws Exception {
        // Step 1: Request verification code
        mockMvc.perform(post("/api/auth/request-verification")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "test@isfce.be"}
                                """))
                .andExpect(status().isAccepted());

        // Verify code was stored in Redis
        String redisValue = redisTemplate.opsForValue().get("verify:" + unverifiedUser.getId());
        assertThat(redisValue).isNotNull();
        assertThat(redisValue).contains(":"); // format: "code:emailHash"
        String code = redisValue.split(":")[0];
        assertThat(code).hasSize(6);

        // Step 2: Confirm with the correct code
        mockMvc.perform(post("/api/auth/confirm-verification")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "%s"}
                                """.formatted(code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        // Verify user is now verified in DB
        User refreshed = userRepository.findById(unverifiedUser.getId()).orElseThrow();
        assertThat(refreshed.isVerified()).isTrue();
        assertThat(refreshed.getEmailHash()).isNotNull();

        // Verify Redis keys are cleaned up
        assertThat(redisTemplate.opsForValue().get("verify:" + unverifiedUser.getId())).isNull();
        assertThat(redisTemplate.opsForValue().get("verify-attempts:" + unverifiedUser.getId())).isNull();
    }

    @Test
    void shouldBlockBruteForceAfterFiveAttempts() throws Exception {
        // First, request a verification code
        mockMvc.perform(post("/api/auth/request-verification")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "brute@isfce.be"}
                                """))
                .andExpect(status().isAccepted());

        // Submit 5 wrong codes
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/confirm-verification")
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"code": "000000"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        // 6th attempt should trigger rate limit (429)
        mockMvc.perform(post("/api/auth/confirm-verification")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "000000"}
                                """))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldRejectNonIsfceEmail() throws Exception {
        mockMvc.perform(post("/api/auth/request-verification")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@gmail.com"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectDuplicateEmailVerification() throws Exception {
        // Create another user already verified with the same email hash
        User existing = createUser("already-verified", true, "USER");
        // Manually set the email hash that "dup@isfce.be" would produce
        String emailHash = be.freenote.util.HashUtil.hashEmail("dup@isfce.be", "test-salt");
        existing.setEmailHash(emailHash);
        userRepository.save(existing);

        // Now try to verify with the same email
        mockMvc.perform(post("/api/auth/request-verification")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "dup@isfce.be"}
                                """))
                .andExpect(status().isConflict());
    }
}

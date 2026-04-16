package be.freenote.integration;

import be.freenote.entity.Donation;
import be.freenote.entity.User;
import be.freenote.entity.UserProfile;
import be.freenote.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the Ko-fi webhook endpoint end-to-end.
 * <p>
 * Important corrections vs the colleague's prompt:
 * - KofiServiceImpl does NOT parse any "luminos:{userId}" tags.
 *   It matches users by email hash OR by username == fromName.
 * - KofiServiceImpl does NOT award badges. It sets adFree + adFreeUntil on UserProfile.
 * - The endpoint always returns 200 (Ko-fi requirement), even on invalid tokens.
 */
@Tag("integration")
class KofiWebhookTest extends AbstractIntegrationTest {

    @MockitoBean private JavaMailSender mailSender;

    private User verifiedUser;

    @BeforeEach
    void setUp() {
        donationRepository.deleteAll();
        ratingRepository.deleteAll();
        favoriteRepository.deleteAll();
        badgeRepository.deleteAll();
        documentRepository.deleteAll();
        courseRepository.deleteAll();
        sectionRepository.deleteAll();
        userRepository.deleteAll();

        verifiedUser = createVerifiedUser("kofi-donor");
        // Set email hash so the webhook can match by email
        String emailHash = HashUtil.hashEmail("donor@isfce.be", "test-salt");
        verifiedUser.setEmailHash(emailHash);
        userRepository.save(verifiedUser);
    }

    @Test
    void shouldActivateAdFreeOnValidWebhookMatchedByEmail() throws Exception {
        String payload = kofiPayload(
                "test-kofi-token",
                "Donation",
                "Some Random Name",
                "donor@isfce.be",
                "5.00",
                "txn-001"
        );

        mockMvc.perform(post("/api/webhooks/kofi")
                        .param("data", payload))
                .andExpect(status().isOk());

        // Verify donation was created
        var donations = donationRepository.findAll();
        assertThat(donations).hasSize(1);
        Donation donation = donations.getFirst();
        assertThat(donation.getAmount()).isEqualByComparingTo("5.00");
        assertThat(donation.getKofiTransactionId()).isEqualTo("txn-001");
        assertThat(donation.getUser().getId()).isEqualTo(verifiedUser.getId());

        // Verify ad-free was activated on UserProfile
        User refreshed = userRepository.findById(verifiedUser.getId()).orElseThrow();
        UserProfile profile = refreshed.getProfile();
        assertThat(profile.isAdFree()).isTrue();
        assertThat(profile.getAdFreeUntil()).isAfter(LocalDateTime.now().plusDays(29));
    }

    @Test
    void shouldMatchUserByUsernameWhenEmailMissing() throws Exception {
        String payload = kofiPayload(
                "test-kofi-token",
                "Donation",
                "kofi-donor",  // matches username
                null,          // no email
                "3.00",
                "txn-002"
        );

        mockMvc.perform(post("/api/webhooks/kofi")
                        .param("data", payload))
                .andExpect(status().isOk());

        var donations = donationRepository.findAll();
        assertThat(donations).hasSize(1);
        assertThat(donations.getFirst().getUser().getId()).isEqualTo(verifiedUser.getId());
    }

    @Test
    void shouldCumulateAdFreeDays() throws Exception {
        // Set existing ad-free period
        UserProfile profile = verifiedUser.getProfile();
        profile.setAdFree(true);
        profile.setAdFreeUntil(LocalDateTime.now().plusDays(15));
        userRepository.save(verifiedUser);

        String payload = kofiPayload(
                "test-kofi-token",
                "Donation",
                "kofi-donor",
                "donor@isfce.be",
                "5.00",
                "txn-003"
        );

        mockMvc.perform(post("/api/webhooks/kofi")
                        .param("data", payload))
                .andExpect(status().isOk());

        // Should be original 15 days + 30 new days = ~45 days from now
        User refreshed = userRepository.findById(verifiedUser.getId()).orElseThrow();
        assertThat(refreshed.getProfile().getAdFreeUntil())
                .isAfter(LocalDateTime.now().plusDays(44));
    }

    @Test
    void shouldReturn200ButDoNothingOnInvalidToken() throws Exception {
        String payload = kofiPayload(
                "wrong-token",
                "Donation",
                "kofi-donor",
                "donor@isfce.be",
                "5.00",
                "txn-004"
        );

        // Ko-fi requires 200 always
        mockMvc.perform(post("/api/webhooks/kofi")
                        .param("data", payload))
                .andExpect(status().isOk());

        // No donation created
        assertThat(donationRepository.findAll()).isEmpty();
        // Profile unchanged
        User refreshed = userRepository.findById(verifiedUser.getId()).orElseThrow();
        assertThat(refreshed.getProfile().isAdFree()).isFalse();
    }

    @Test
    void shouldIgnoreNonDonationType() throws Exception {
        String payload = kofiPayload(
                "test-kofi-token",
                "Shop Order",
                "kofi-donor",
                "donor@isfce.be",
                "10.00",
                "txn-005"
        );

        mockMvc.perform(post("/api/webhooks/kofi")
                        .param("data", payload))
                .andExpect(status().isOk());

        assertThat(donationRepository.findAll()).isEmpty();
    }

    @Test
    void shouldCreateDonationEvenWhenUserNotMatched() throws Exception {
        String payload = kofiPayload(
                "test-kofi-token",
                "Donation",
                "Unknown Person",
                "stranger@example.com",
                "7.00",
                "txn-006"
        );

        mockMvc.perform(post("/api/webhooks/kofi")
                        .param("data", payload))
                .andExpect(status().isOk());

        // Donation is created with user = null
        var donations = donationRepository.findAll();
        assertThat(donations).hasSize(1);
        assertThat(donations.getFirst().getUser()).isNull();
        assertThat(donations.getFirst().getAmount()).isEqualByComparingTo("7.00");
    }

    // --- Helper ---

    private String kofiPayload(String token, String type, String fromName,
                                String email, String amount, String txnId) {
        // Build JSON matching KofiWebhookPayload fields
        String emailField = email != null
                ? "\"email\": \"%s\",".formatted(email)
                : "\"email\": null,";
        return """
                {
                    "verification_token": "%s",
                    "type": "%s",
                    "from_name": "%s",
                    %s
                    "amount": "%s",
                    "currency": "EUR",
                    "kofi_transaction_id": "%s",
                    "message_id": "msg-001",
                    "timestamp": "2026-04-12T10:00:00Z",
                    "is_public": true,
                    "message": "Keep up the good work!"
                }
                """.formatted(token, type, fromName, emailField, amount, txnId);
    }
}

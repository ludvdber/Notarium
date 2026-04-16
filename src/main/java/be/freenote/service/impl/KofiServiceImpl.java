package be.freenote.service.impl;

import be.freenote.dto.request.KofiWebhookPayload;
import be.freenote.entity.Donation;
import be.freenote.entity.User;
import be.freenote.entity.UserProfile;
import be.freenote.repository.DonationRepository;
import be.freenote.repository.UserRepository;
import be.freenote.service.KofiService;
import be.freenote.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KofiServiceImpl implements KofiService {

    private final UserRepository userRepository;
    private final DonationRepository donationRepository;

    @Value("${app.kofi.verification-token:}")
    private String expectedToken;

    @Value("${app.email.hash-salt}")
    private String emailHashSalt;

    private static final int AD_FREE_DAYS_PER_DONATION = 30;

    @Override
    @Transactional
    public void processWebhook(KofiWebhookPayload payload) {
        if (expectedToken.isBlank() || !expectedToken.equals(payload.getVerificationToken())) {
            log.warn("Ko-fi webhook rejected: invalid verification token");
            return;
        }

        if (!"Donation".equals(payload.getType()) && !"Subscription".equals(payload.getType())) {
            log.info("Ko-fi webhook ignored: type={}", payload.getType());
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(payload.getAmount());
        } catch (NumberFormatException e) {
            log.warn("Ko-fi webhook: invalid amount '{}'", payload.getAmount());
            return;
        }

        // Try to match user by email hash
        Optional<User> userOpt = Optional.empty();
        if (payload.getEmail() != null && !payload.getEmail().isBlank()) {
            String hash = HashUtil.hashEmail(payload.getEmail(), emailHashSalt);
            userOpt = userRepository.findByEmailHash(hash);
        }

        // If no match by email, try by Ko-fi display name == username
        if (userOpt.isEmpty() && payload.getFromName() != null) {
            userOpt = userRepository.findByUsername(payload.getFromName());
        }

        User user = userOpt.orElse(null);

        LocalDateTime adFreeUntil = LocalDateTime.now().plusDays(AD_FREE_DAYS_PER_DONATION);

        Donation donation = Donation.builder()
                .user(user)
                .amount(amount)
                .kofiTransactionId(payload.getKofiTransactionId())
                .adFreeUntil(adFreeUntil)
                .build();
        donationRepository.save(donation);

        if (user != null && user.getProfile() != null) {
            UserProfile profile = user.getProfile();
            // Extend ad-free period: max of current expiry or new grant
            if (profile.getAdFreeUntil() != null && profile.getAdFreeUntil().isAfter(LocalDateTime.now())) {
                profile.setAdFreeUntil(profile.getAdFreeUntil().plusDays(AD_FREE_DAYS_PER_DONATION));
            } else {
                profile.setAdFreeUntil(adFreeUntil);
            }
            profile.setAdFree(true);
            log.info("Ko-fi donation processed: user={}, amount={}, ad-free until {}",
                    user.getUsername(), amount, profile.getAdFreeUntil());
        } else {
            log.info("Ko-fi donation processed: unmatched donor '{}', amount={}, transaction={}",
                    payload.getFromName(), amount, payload.getKofiTransactionId());
        }
    }
}

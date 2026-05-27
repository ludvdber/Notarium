package be.freenote.service.impl;

import be.freenote.dto.response.DonationResponse;
import be.freenote.dto.response.PageResponse;
import be.freenote.entity.Donation;
import be.freenote.entity.User;
import be.freenote.entity.UserProfile;
import be.freenote.repository.DonationRepository;
import be.freenote.repository.UserRepository;
import be.freenote.repository.Repositories;
import be.freenote.service.DonationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DonationResponse> listAll(Pageable pageable) {
        Page<Donation> page = donationRepository.findAllByOrderByIdDesc(pageable);
        List<DonationResponse> content = page.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    @Transactional
    public DonationResponse grantAdFree(Long targetUserId, int days, Long actingAdminId) {
        if (days <= 0 || days > 3650) {
            throw new IllegalArgumentException("days must be between 1 and 3650");
        }
        User user = Repositories.findByIdOrThrow(userRepository, targetUserId, "User");
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
            user.setProfile(profile);
        }

        // Extend existing period if still active, otherwise start fresh from now.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = profile.getAdFreeUntil() != null && profile.getAdFreeUntil().isAfter(now)
                ? profile.getAdFreeUntil()
                : now;
        LocalDateTime newExpiry = base.plusDays(days);
        profile.setAdFreeUntil(newExpiry);
        profile.setAdFree(true);

        Donation audit = Donation.builder()
                .user(user)
                .amount(BigDecimal.ZERO)
                .kofiTransactionId("MANUAL-" + actingAdminId + "-" + System.currentTimeMillis())
                .adFreeUntil(newExpiry)
                .build();
        donationRepository.save(audit);

        log.info("Admin {} granted {} ad-free days to user {} (until {})", actingAdminId, days, user.getUsername(), newExpiry);
        return toResponse(audit);
    }

    private DonationResponse toResponse(Donation d) {
        User u = d.getUser();
        return new DonationResponse(
                d.getId(),
                u != null ? u.getId() : null,
                u != null ? u.getUsername() : null,
                d.getAmount(),
                d.getKofiTransactionId(),
                d.getAdFreeUntil()
        );
    }
}

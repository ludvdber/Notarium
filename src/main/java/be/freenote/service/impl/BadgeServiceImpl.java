package be.freenote.service.impl;

import be.freenote.entity.Badge;
import be.freenote.entity.User;
import be.freenote.repository.BadgeRepository;
import be.freenote.repository.DocumentRepository;
import be.freenote.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private static final Map<String, Integer> DOC_THRESHOLDS = Map.of(
            "FIRST_UPLOAD", 1,
            "CONTRIBUTOR_10", 10,
            "CONTRIBUTOR_50", 50,
            "CONTRIBUTOR_100", 100
    );

    private static final Map<String, Integer> XP_THRESHOLDS = Map.of(
            "XP_100", 100,
            "XP_500", 500,
            "XP_1000", 1000
    );

    private final BadgeRepository badgeRepository;
    private final DocumentRepository documentRepository;

    @Override
    @Transactional
    public void checkAndAwardBadges(User user) {
        Set<String> existing = user.getBadges().stream()
                .map(Badge::getBadgeType)
                .collect(Collectors.toSet());

        long docCount = documentRepository.countByUserId(user.getId());
        int xp = user.getXp();

        DOC_THRESHOLDS.forEach((badgeType, threshold) -> {
            if (docCount >= threshold && !existing.contains(badgeType)) {
                awardBadge(user, badgeType);
            }
        });

        XP_THRESHOLDS.forEach((badgeType, threshold) -> {
            if (xp >= threshold && !existing.contains(badgeType)) {
                awardBadge(user, badgeType);
            }
        });
    }

    private void awardBadge(User user, String badgeType) {
        Badge badge = Badge.builder()
                .user(user)
                .badgeType(badgeType)
                .build();
        badgeRepository.save(badge);
        user.getBadges().add(badge);
    }
}

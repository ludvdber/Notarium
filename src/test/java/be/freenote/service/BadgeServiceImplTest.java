package be.freenote.service;

import be.freenote.entity.Badge;
import be.freenote.entity.User;
import be.freenote.repository.BadgeRepository;
import be.freenote.repository.DocumentRepository;
import be.freenote.service.impl.BadgeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceImplTest {

    @Mock private BadgeRepository badgeRepository;
    @Mock private DocumentRepository documentRepository;

    @InjectMocks private BadgeServiceImpl badgeService;

    private User userWithBadges(int xp, List<String> existingBadges) {
        User user = User.builder().id(1L).username("test").xp(xp).badges(new ArrayList<>()).build();
        existingBadges.forEach(type -> user.getBadges().add(
                Badge.builder().user(user).badgeType(type).build()));
        return user;
    }

    @Test
    void shouldAwardFirstUploadBadge() {
        User user = userWithBadges(0, List.of());
        when(documentRepository.countByUserId(1L)).thenReturn(1L);

        badgeService.checkAndAwardBadges(user);

        ArgumentCaptor<Badge> captor = ArgumentCaptor.forClass(Badge.class);
        verify(badgeRepository, atLeastOnce()).save(captor.capture());
        List<String> awarded = captor.getAllValues().stream().map(Badge::getBadgeType).toList();
        assertThat(awarded).contains("FIRST_UPLOAD");
    }

    @Test
    void shouldAwardMultipleDocBadgesAtOnce() {
        User user = userWithBadges(0, List.of());
        when(documentRepository.countByUserId(1L)).thenReturn(50L);

        badgeService.checkAndAwardBadges(user);

        ArgumentCaptor<Badge> captor = ArgumentCaptor.forClass(Badge.class);
        verify(badgeRepository, atLeast(3)).save(captor.capture());
        List<String> awarded = captor.getAllValues().stream().map(Badge::getBadgeType).toList();
        assertThat(awarded).contains("FIRST_UPLOAD", "CONTRIBUTOR_10", "CONTRIBUTOR_50");
    }

    @Test
    void shouldAwardXpBadges() {
        User user = userWithBadges(500, List.of());
        when(documentRepository.countByUserId(1L)).thenReturn(0L);

        badgeService.checkAndAwardBadges(user);

        ArgumentCaptor<Badge> captor = ArgumentCaptor.forClass(Badge.class);
        verify(badgeRepository, atLeast(2)).save(captor.capture());
        List<String> awarded = captor.getAllValues().stream().map(Badge::getBadgeType).toList();
        assertThat(awarded).contains("XP_100", "XP_500");
        assertThat(awarded).doesNotContain("XP_1000");
    }

    @Test
    void shouldNotDuplicateExistingBadges() {
        // 500 XP + 10 docs → eligible for XP_100, XP_500, FIRST_UPLOAD, CONTRIBUTOR_10
        // but XP_100 and FIRST_UPLOAD already exist → only XP_500 and CONTRIBUTOR_10 awarded
        User user = userWithBadges(500, List.of("XP_100", "FIRST_UPLOAD"));
        when(documentRepository.countByUserId(1L)).thenReturn(10L);

        badgeService.checkAndAwardBadges(user);

        ArgumentCaptor<Badge> captor = ArgumentCaptor.forClass(Badge.class);
        verify(badgeRepository, atLeastOnce()).save(captor.capture());
        List<String> awarded = captor.getAllValues().stream().map(Badge::getBadgeType).toList();
        assertThat(awarded).contains("XP_500", "CONTRIBUTOR_10");
        assertThat(awarded).doesNotContain("XP_100", "FIRST_UPLOAD"); // already existed
    }

    @Test
    void shouldNotAwardAnyBadgesWhenNoneQualified() {
        User user = userWithBadges(10, List.of());
        when(documentRepository.countByUserId(1L)).thenReturn(0L);

        badgeService.checkAndAwardBadges(user);

        verify(badgeRepository, never()).save(any());
    }

    @Test
    void shouldAwardContributor100Badge() {
        User user = userWithBadges(0, List.of("FIRST_UPLOAD", "CONTRIBUTOR_10", "CONTRIBUTOR_50"));
        when(documentRepository.countByUserId(1L)).thenReturn(100L);

        badgeService.checkAndAwardBadges(user);

        ArgumentCaptor<Badge> captor = ArgumentCaptor.forClass(Badge.class);
        verify(badgeRepository).save(captor.capture());
        assertThat(captor.getValue().getBadgeType()).isEqualTo("CONTRIBUTOR_100");
    }
}

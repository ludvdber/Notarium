package be.freenote.service.impl;

import be.freenote.dto.request.UpdateProfileRequest;
import be.freenote.dto.response.LeaderboardEntry;
import be.freenote.dto.response.ProfileCardResponse;
import be.freenote.dto.response.UserResponse;
import be.freenote.entity.User;
import be.freenote.entity.UserProfile;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.mapper.UserMapper;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.ReportRepository;
import be.freenote.repository.UserRepository;
import be.freenote.service.BadgeService;
import be.freenote.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final ReportRepository reportRepository;
    private final UserMapper userMapper;
    private final BadgeService badgeService;

    @Override
    public UserResponse getProfile(Long userId) {
        User user = findUserOrThrow(userId);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getPublicProfile(Long userId) {
        User user = findUserOrThrow(userId);
        return userMapper.toPublicResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserOrThrow(userId);

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
            user.setProfile(profile);
        }

        profile.setBio(request.getBio());
        profile.setWebsite(request.getWebsite());
        profile.setGithub(request.getGithub());
        profile.setLinkedin(request.getLinkedin());
        profile.setDiscord(request.getDiscord());
        profile.setProfilePublic(request.isProfilePublic());
        // showInCarousel is admin-only — regular users cannot feature themselves on the homepage
        if ("ADMIN".equals(user.getRole())) {
            profile.setShowInCarousel(request.isShowInCarousel());
        }
        if (request.getThemePref() != null) {
            profile.setThemePref(request.getThemePref());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard(int size) {
        AtomicInteger rank = new AtomicInteger(1);
        return userRepository.findAllByOrderByXpDesc(PageRequest.of(0, size)).stream()
                .map(user -> userMapper.toLeaderboardEntry(user, rank.getAndIncrement()))
                .toList();
    }

    @Override
    public List<ProfileCardResponse> getFeaturedProfiles() {
        return userRepository.findFeaturedProfiles().stream()
                .map(userMapper::toProfileCard)
                .toList();
    }

    @Override
    @Transactional
    public void addXp(Long userId, int amount) {
        User user = findUserOrThrow(userId);
        user.setXp(user.getXp() + amount);
        userRepository.save(user);
        badgeService.checkAndAwardBadges(user);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId) {
        User user = findUserOrThrow(userId);

        // Detach reports so pending ones survive for admin review
        var reports = reportRepository.findByUserId(userId);
        for (var report : reports) {
            report.setUser(null);
        }
        reportRepository.saveAll(reports);

        // Anonymize documents before deletion — SET NULL via cascade handles FK,
        // but we also mark them anonymous for display
        documentRepository.anonymizeByUserId(userId);

        userRepository.delete(user);

        log.info("Account deleted: userId={}, documents anonymized, {} reports detached",
                userId, reports.size());
    }

    @Override
    @Transactional
    public void acceptTerms(Long userId) {
        User user = findUserOrThrow(userId);
        UserProfile profile = user.getProfile();
        if (profile == null) {
            throw new ResourceNotFoundException("UserProfile", "userId", userId);
        }
        if (profile.getTermsAcceptedAt() != null) {
            return; // already accepted — idempotent
        }
        profile.setTermsAcceptedAt(java.time.LocalDateTime.now());
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}

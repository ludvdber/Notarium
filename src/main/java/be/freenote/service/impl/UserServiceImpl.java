package be.freenote.service.impl;

import be.freenote.dto.request.UpdateProfileRequest;
import be.freenote.dto.response.LeaderboardEntry;
import be.freenote.dto.response.ProfileCardResponse;
import be.freenote.dto.response.UserResponse;
import be.freenote.entity.User;
import be.freenote.entity.UserProfile;
import be.freenote.enums.AvatarSource;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.mapper.UserMapper;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.Repositories;
import be.freenote.repository.ReportRepository;
import be.freenote.repository.UserRepository;
import be.freenote.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final ReportRepository reportRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getProfile(Long userId) {
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");
        long docCount = documentRepository.countByUserId(userId);
        return userMapper.toResponse(user, docCount);
    }

    @Override
    public UserResponse getPublicProfile(Long userId) {
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");
        long docCount = documentRepository.countByUserId(userId);
        return userMapper.toPublicResponse(user, docCount);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
            user.setProfile(profile);
        }

        profile.setBio(sanitize(request.getBio()));
        profile.setWebsite(sanitize(request.getWebsite()));
        profile.setGithub(sanitize(request.getGithub()));
        profile.setLinkedin(sanitize(request.getLinkedin()));
        profile.setDiscord(sanitize(request.getDiscord()));
        profile.setProfilePublic(request.isProfilePublic());
        // Verified users (and admins) can opt in/out of the homepage carousel themselves.
        // Unverified accounts cannot — keeps the carousel away from throwaway accounts.
        if (user.isVerified() || "ADMIN".equals(user.getRole())) {
            profile.setShowInCarousel(request.isShowInCarousel());
        }
        if (request.getAvatarSource() != null) {
            profile.setAvatarSource(AvatarSource.valueOf(request.getAvatarSource()));
        }
        profile.setFirstName(sanitize(request.getFirstName()));
        profile.setLastName(sanitize(request.getLastName()));
        profile.setDisplayRealName(request.isDisplayRealName());

        User saved = userRepository.save(user);
        long docCount = documentRepository.countByUserId(userId);
        return userMapper.toResponse(saved, docCount);
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard(int size) {
        List<User> users = userRepository.findAllByOrderByXpDesc(PageRequest.of(0, size));

        // Batch fetch document counts — 1 query instead of N
        Map<Long, Long> docCounts = batchDocCounts(users);

        AtomicInteger rank = new AtomicInteger(1);
        return users.stream()
                .map(user -> userMapper.toLeaderboardEntry(
                        user, rank.getAndIncrement(),
                        docCounts.getOrDefault(user.getId(), 0L)))
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
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");
        user.setXp(user.getXp() + amount);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId) {
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");

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
    public void adminDeleteUser(Long userId) {
        // Same data lifecycle as a self-delete: anonymize documents, detach reports, drop the row.
        // The caller (admin endpoint) doesn't need to revoke a JWT since it's not the admin's own session.
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");
        var reports = reportRepository.findByUserId(userId);
        for (var report : reports) {
            report.setUser(null);
        }
        reportRepository.saveAll(reports);
        documentRepository.anonymizeByUserId(userId);
        userRepository.delete(user);
        log.info("Admin deleted account: userId={}, username={}, documents anonymized",
                userId, user.getUsername());
    }

    @Override
    @Transactional
    public void acceptTerms(Long userId) {
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");
        UserProfile profile = user.getProfile();
        if (profile == null) {
            throw new ResourceNotFoundException("UserProfile", "userId", userId);
        }
        if (profile.getTermsAcceptedAt() != null) {
            return; // already accepted — idempotent
        }
        profile.setTermsAcceptedAt(java.time.LocalDateTime.now());
    }

    @Override
    public List<UserResponse> adminSearchUsers(String query, int limit) {
        int clamped = Math.min(Math.max(limit, 1), 100);
        String q = query == null ? "" : query.trim();
        List<User> users = userRepository.searchByUsername(q, PageRequest.of(0, clamped));
        Map<Long, Long> docCounts = batchDocCounts(users);
        return users.stream()
                .map(u -> userMapper.toResponse(u, docCounts.getOrDefault(u.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional
    public UserResponse adminVerifyUser(Long userId) {
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");
        user.setVerified(true);
        if ("USER".equals(user.getRole())) {
            user.setRole("VERIFIED");
        }
        User saved = userRepository.save(user);
        long docCount = documentRepository.countByUserId(userId);
        log.info("Admin manually verified user: id={}, username={}", userId, user.getUsername());
        return userMapper.toResponse(saved, docCount);
    }

    @Override
    @Transactional
    public UserResponse adminUnverifyUser(Long userId) {
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");
        user.setVerified(false);
        if ("VERIFIED".equals(user.getRole())) {
            user.setRole("USER");
        }
        User saved = userRepository.save(user);
        long docCount = documentRepository.countByUserId(userId);
        log.info("Admin revoked verification for user: id={}, username={}", userId, user.getUsername());
        return userMapper.toResponse(saved, docCount);
    }

    @Override
    @Transactional
    public UserResponse adminUpdateRole(Long userId, String role) {
        if (role == null || !(role.equals("USER") || role.equals("VERIFIED") || role.equals("ADMIN"))) {
            throw new IllegalArgumentException("Role must be USER, VERIFIED or ADMIN");
        }
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");
        user.setRole(role);
        if (role.equals("VERIFIED") || role.equals("ADMIN")) {
            user.setVerified(true);
        }
        User saved = userRepository.save(user);
        long docCount = documentRepository.countByUserId(userId);
        log.info("Admin updated role for user: id={}, role={}", userId, role);
        return userMapper.toResponse(saved, docCount);
    }

    /** Fetches document counts for a list of users in a single query. */
    private Map<Long, Long> batchDocCounts(List<User> users) {
        List<Long> ids = users.stream().map(User::getId).toList();
        if (ids.isEmpty()) return Map.of();
        return documentRepository.countByUserIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private static String sanitize(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}

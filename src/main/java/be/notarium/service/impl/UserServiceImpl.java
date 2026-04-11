package be.notarium.service.impl;

import be.notarium.dto.request.UpdateProfileRequest;
import be.notarium.dto.response.LeaderboardEntry;
import be.notarium.dto.response.ProfileCardResponse;
import be.notarium.dto.response.UserResponse;
import be.notarium.entity.User;
import be.notarium.entity.UserProfile;
import be.notarium.exception.ResourceNotFoundException;
import be.notarium.mapper.UserMapper;
import be.notarium.repository.DocumentRepository;
import be.notarium.repository.UserRepository;
import be.notarium.service.BadgeService;
import be.notarium.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final UserMapper userMapper;
    private final BadgeService badgeService;

    @Override
    public UserResponse getProfile(Long userId) {
        User user = findUserOrThrow(userId);
        return userMapper.toResponse(user);
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
        profile.setShowInCarousel(request.isShowInCarousel());
        if (request.getThemePref() != null) {
            profile.setThemePref(request.getThemePref());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard() {
        AtomicInteger rank = new AtomicInteger(1);
        return userRepository.findTop10ByOrderByXpDesc().stream()
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

        // Anonymize documents before deletion — SET NULL via cascade handles FK,
        // but we also mark them anonymous for display
        documentRepository.anonymizeByUserId(userId);

        userRepository.delete(user);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}

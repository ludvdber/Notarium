package be.freenote.service;

import be.freenote.dto.request.UpdateProfileRequest;
import be.freenote.dto.response.LeaderboardEntry;
import be.freenote.dto.response.ProfileCardResponse;
import be.freenote.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getProfile(Long userId);
    UserResponse getPublicProfile(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    List<LeaderboardEntry> getLeaderboard(int size);
    List<ProfileCardResponse> getFeaturedProfiles();
    void addXp(Long userId, int amount);
    void deleteAccount(Long userId);
    void acceptTerms(Long userId);
}

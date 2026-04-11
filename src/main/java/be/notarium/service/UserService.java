package be.notarium.service;

import be.notarium.dto.request.UpdateProfileRequest;
import be.notarium.dto.response.LeaderboardEntry;
import be.notarium.dto.response.ProfileCardResponse;
import be.notarium.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getProfile(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    List<LeaderboardEntry> getLeaderboard();
    List<ProfileCardResponse> getFeaturedProfiles();
    void addXp(Long userId, int amount);
    void deleteAccount(Long userId);
}

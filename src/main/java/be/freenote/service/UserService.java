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
    /** Admin: list all users with basic profile info, paginated & searchable. */
    List<UserResponse> adminSearchUsers(String query, int limit);
    /** Admin: mark a user as verified (bypasses the @isfce.be email flow). */
    UserResponse adminVerifyUser(Long userId);
    /** Admin: revoke the verified flag. */
    UserResponse adminUnverifyUser(Long userId);
    /** Admin: update the role of a user (USER, VERIFIED, ADMIN). */
    UserResponse adminUpdateRole(Long userId, String role);
    /** Admin: delete a user account. Documents are anonymized (kept) — same semantics as self-deletion. */
    void adminDeleteUser(Long userId);
}

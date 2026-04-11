package be.notarium.controller;

import be.notarium.dto.request.UpdateProfileRequest;
import be.notarium.dto.response.ProfileCardResponse;
import be.notarium.dto.response.UserResponse;
import be.notarium.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile",
               description = "Returns the full profile of the authenticated user.")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile",
               description = "Updates the authenticated user's profile fields (bio, social links, theme, etc.).")
    public ResponseEntity<UserResponse> updateProfile(Authentication authentication,
                                                       @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user public profile",
               description = "Returns the public profile of a user by ID.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured profiles",
               description = "Returns the list of users marked for the homepage carousel.")
    public ResponseEntity<List<ProfileCardResponse>> getFeaturedProfiles() {
        return ResponseEntity.ok(userService.getFeaturedProfiles());
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete account",
               description = "Anonymizes the user's documents and permanently deletes the account.")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }
}

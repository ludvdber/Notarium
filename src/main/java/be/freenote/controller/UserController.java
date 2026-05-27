package be.freenote.controller;

import be.freenote.security.SecurityUtils;
import be.freenote.dto.request.UpdateProfileRequest;
import be.freenote.dto.response.DocumentResponse;
import be.freenote.dto.response.ProfileCardResponse;
import be.freenote.dto.response.UserResponse;
import be.freenote.security.JwtRevocationService;
import be.freenote.security.JwtTokenProvider;
import be.freenote.security.OAuth2LoginSuccessHandler;
import be.freenote.service.RecentDocsService;
import be.freenote.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final RecentDocsService recentDocsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRevocationService jwtRevocationService;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile",
               description = "Returns the full profile of the authenticated user.")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        Long userId = SecurityUtils.currentUserId(authentication);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile",
               description = "Updates the authenticated user's profile fields (bio, social links, theme, etc.).")
    public ResponseEntity<UserResponse> updateProfile(Authentication authentication,
                                                       @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.currentUserId(authentication);
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user public profile",
               description = "Returns the public profile of a user by ID. Private profiles return limited data (no bio/social links).")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicProfile(id));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured profiles",
               description = "Returns the list of users marked for the homepage carousel.")
    public ResponseEntity<List<ProfileCardResponse>> getFeaturedProfiles() {
        return ResponseEntity.ok(userService.getFeaturedProfiles());
    }

    @GetMapping("/me/recent-docs")
    @Operation(summary = "List recently opened documents",
               description = "Returns the last opened verified documents for the current user, most recent first.")
    public ResponseEntity<List<DocumentResponse>> getRecentDocs(Authentication authentication,
                                                                 @RequestParam(defaultValue = "6") int limit) {
        Long userId = SecurityUtils.currentUserId(authentication);
        return ResponseEntity.ok(recentDocsService.getRecent(userId, limit));
    }

    @PostMapping("/me/recent-docs/{docId}")
    @Operation(summary = "Record a document visit",
               description = "Bumps the document to the top of the user's \"recently opened\" trail. Idempotent.")
    public ResponseEntity<Void> recordVisit(Authentication authentication, @PathVariable Long docId) {
        Long userId = SecurityUtils.currentUserId(authentication);
        recentDocsService.recordVisit(userId, docId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/accept-terms")
    @Operation(summary = "Accept Terms of Service",
               description = "Records the user's explicit acceptance of the Terms of Service and Privacy Policy. Idempotent.")
    public ResponseEntity<Void> acceptTerms(Authentication authentication) {
        Long userId = SecurityUtils.currentUserId(authentication);
        userService.acceptTerms(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete account",
               description = "Anonymizes the user's documents, permanently deletes the account, revokes the JWT and clears the cookie.")
    public ResponseEntity<Void> deleteAccount(Authentication authentication,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        Long userId = SecurityUtils.currentUserId(authentication);
        userService.deleteAccount(userId);

        // Revoke the JWT tied to this session so it cannot be reused while still within its TTL.
        var cookies = request.getCookies();
        if (cookies != null) {
            for (var c : cookies) {
                if ("jwt".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()
                        && jwtTokenProvider.validateToken(c.getValue())) {
                    jwtRevocationService.revoke(
                            jwtTokenProvider.getJti(c.getValue()),
                            jwtTokenProvider.getExpiration(c.getValue()));
                    break;
                }
            }
        }

        OAuth2LoginSuccessHandler.clearJwtCookie(response, cookieSecure);
        return ResponseEntity.noContent().build();
    }
}

package be.freenote.controller;

import be.freenote.dto.request.ConfirmCodeRequest;
import be.freenote.dto.request.VerifyEmailRequest;
import be.freenote.security.OAuth2LoginSuccessHandler;
import be.freenote.security.ratelimit.RateLimit;
import be.freenote.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OAuth2 login and email verification")
public class AuthController {

    private final AuthService authService;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @PostMapping("/request-verification")
    @RateLimit(max = 3, window = 3600)
    @Operation(summary = "Request email verification",
               description = "Sends a 6-digit verification code to the provided ISFCE email.")
    public ResponseEntity<Void> requestVerification(Authentication authentication,
                                                     @Valid @RequestBody VerifyEmailRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        authService.requestVerification(userId, request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/confirm-verification")
    @RateLimit(max = 5, window = 900)
    @Operation(summary = "Confirm email verification",
               description = "Validates the 6-digit code and refreshes the HttpOnly JWT cookie with verified=true.")
    public ResponseEntity<Void> confirmVerification(Authentication authentication,
                                                     @Valid @RequestBody ConfirmCodeRequest request,
                                                     HttpServletResponse response) {
        Long userId = (Long) authentication.getPrincipal();
        String jwt = authService.confirmVerification(userId, request.getCode());
        OAuth2LoginSuccessHandler.addJwtCookie(response, jwt, jwtExpirationMs);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Clears the JWT cookie.")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        OAuth2LoginSuccessHandler.clearJwtCookie(response);
        return ResponseEntity.noContent().build();
    }
}

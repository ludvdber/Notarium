package be.notarium.controller;

import be.notarium.dto.request.ConfirmCodeRequest;
import be.notarium.dto.request.VerifyEmailRequest;
import be.notarium.security.ratelimit.RateLimit;
import be.notarium.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OAuth2 login and email verification")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/request-verification")
    @RateLimit(max = 3, window = 3600)
    @Operation(summary = "Request email verification",
               description = "Sends a 6-digit verification code to the provided ISFCE email. Returns 409 if the email is already verified by another account.")
    public ResponseEntity<Void> requestVerification(Authentication authentication,
                                                     @Valid @RequestBody VerifyEmailRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        authService.requestVerification(userId, request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/confirm-verification")
    @RateLimit(max = 5, window = 900)
    @Operation(summary = "Confirm email verification",
               description = "Validates the 6-digit code sent by email and returns a new JWT with verified=true.")
    public ResponseEntity<Map<String, String>> confirmVerification(Authentication authentication,
                                                                    @Valid @RequestBody ConfirmCodeRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        String jwt = authService.confirmVerification(userId, request.getCode());
        return ResponseEntity.ok(Map.of("token", jwt));
    }
}

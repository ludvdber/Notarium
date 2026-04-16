package be.freenote.seed;

import be.freenote.repository.UserRepository;
import be.freenote.security.JwtTokenProvider;
import be.freenote.security.OAuth2LoginSuccessHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * DEV-ONLY controller that bypasses OAuth2 and logs in as any seeded user.
 * Does not exist when running in production.
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Profile("dev")
@Tag(name = "Dev", description = "Dev-only endpoints — not available in production")
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @PostMapping("/login/{username}")
    @Operation(summary = "Dev login", description = "Logs in as any user by username. Sets the JWT cookie. Dev profile only.")
    public ResponseEntity<Map<String, String>> devLogin(@PathVariable String username,
                                                         HttpServletResponse response) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        String jwt = jwtTokenProvider.generateToken(user);
        OAuth2LoginSuccessHandler.addJwtCookie(response, jwt, jwtExpirationMs);

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole(),
                "verified", String.valueOf(user.isVerified())
        ));
    }
}

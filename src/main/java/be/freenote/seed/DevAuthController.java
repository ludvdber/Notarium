package be.freenote.seed;

import be.freenote.repository.UserRepository;
import be.freenote.security.JwtTokenProvider;
import be.freenote.security.OAuth2LoginSuccessHandler;
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
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @PostMapping("/login/{username}")
    public ResponseEntity<Map<String, String>> devLogin(@PathVariable String username,
                                                         HttpServletResponse response) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        String jwt = jwtTokenProvider.generateToken(user);
        OAuth2LoginSuccessHandler.addJwtCookie(response, jwt, jwtExpirationMs, cookieSecure);

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole(),
                "verified", String.valueOf(user.isVerified())
        ));
    }
}

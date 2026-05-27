package be.freenote.security;

import be.freenote.entity.User;
import be.freenote.repository.UserOauthLinkRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserOauthLinkRepository oauthLinkRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();
        String oauthId = oauthToken.getPrincipal().getName();

        // If the user was already authenticated, this was a linking flow — keep their session
        // (do NOT rotate the JWT) and bounce them back to /profile with a success flag.
        Long currentUserId = readUserIdFromExistingCookie(request);
        if (currentUserId != null) {
            getRedirectStrategy().sendRedirect(request, response,
                    frontendUrl + "/profile?linked=" + provider);
            return;
        }

        // Normal sign-in / sign-up flow: look up the user via the freshly-linked (provider, oauthId)
        // pair and issue a new JWT cookie.
        User user = oauthLinkRepository.findByProviderAndOauthId(provider, oauthId)
                .map(link -> link.getUser())
                .orElseThrow();

        String jwt = jwtTokenProvider.generateToken(user);
        addJwtCookie(response, jwt, expirationMs, cookieSecure);
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/");
    }

    private Long readUserIdFromExistingCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if ("jwt".equals(c.getName()) && jwtTokenProvider.validateToken(c.getValue())) {
                return jwtTokenProvider.getUserIdFromToken(c.getValue());
            }
        }
        return null;
    }

    public static void addJwtCookie(HttpServletResponse response, String jwt, long expirationMs, boolean secure) {
        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge((int) (expirationMs / 1000));
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    public static void clearJwtCookie(HttpServletResponse response, boolean secure) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }
}

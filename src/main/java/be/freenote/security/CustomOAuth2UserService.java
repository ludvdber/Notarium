package be.freenote.security;

import be.freenote.exception.DuplicateResourceException;
import be.freenote.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // If the caller is already authenticated (their JWT cookie is still valid), this OAuth
        // round-trip is a *linking* flow initiated from /profile, not a sign-in. Attach the
        // (provider, oauthId) to the current account instead of creating a second account.
        Long currentUserId = readCurrentUserIdFromCookie();
        if (currentUserId != null) {
            try {
                authService.linkProvider(currentUserId, oAuth2User, registrationId);
            } catch (DuplicateResourceException e) {
                // Surface as an OAuth error so Spring Security routes to its failure handler
                // (clean redirect) instead of letting it bubble up as a raw 500.
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("link_conflict", e.getMessage(), null), e);
            }
        } else {
            authService.processOAuth2Login(oAuth2User, registrationId);
        }
        return oAuth2User;
    }

    private Long readCurrentUserIdFromCookie() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest req = attrs.getRequest();
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if ("jwt".equals(c.getName()) && jwtTokenProvider.validateToken(c.getValue())) {
                return jwtTokenProvider.getUserIdFromToken(c.getValue());
            }
        }
        return null;
    }
}

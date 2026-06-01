package be.freenote.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Redirects a failed OAuth2 login back to the SPA with a {@code ?authError=<code>} query param
 * instead of Spring's default {@code /login?error} (which the SPA serves as a blank home page).
 * The SPA reads the param on mount and surfaces a toast.
 *
 * <p>The error code is derived from the {@link OAuth2AuthenticationException} raised in
 * {@code AuthServiceImpl} / {@code CustomOAuth2UserService}: a banned Discord identity or an
 * unverified provider email each gets a specific message; anything else is a generic failure.
 */
@Slf4j
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String code = "oauth_failed";
        if (exception instanceof OAuth2AuthenticationException oae && oae.getError() != null) {
            String errorCode = oae.getError().getErrorCode();
            if ("account_banned".equals(errorCode)) {
                code = "banned";
            } else if ("unverified_email".equals(errorCode)) {
                code = "unverified_email";
            }
        }
        log.info("OAuth2 login failed (code={}): {}", code, exception.getMessage());
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/?authError=" + code);
    }
}

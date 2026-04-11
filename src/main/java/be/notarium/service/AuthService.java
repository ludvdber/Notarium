package be.notarium.service;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AuthService {
    String processOAuth2Login(OAuth2User oAuth2User, String registrationId);
    void requestVerification(Long userId, String email);
    String confirmVerification(Long userId, String code);
}

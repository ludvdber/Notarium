package be.freenote.service;

import be.freenote.dto.response.LinkedProviderResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;

public interface AuthService {
    String processOAuth2Login(OAuth2User oAuth2User, String registrationId);
    void linkProvider(Long currentUserId, OAuth2User oAuth2User, String registrationId);
    List<LinkedProviderResponse> getLinkedProviders(Long userId);
    void unlinkProvider(Long userId, String provider);
    void requestVerification(Long userId, String email);
    String confirmVerification(Long userId, String code);
}

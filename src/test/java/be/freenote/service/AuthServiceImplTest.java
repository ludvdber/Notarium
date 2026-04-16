package be.freenote.service;

import be.freenote.entity.User;
import be.freenote.exception.DuplicateResourceException;
import be.freenote.exception.RateLimitExceededException;
import be.freenote.exception.UnauthorizedException;
import be.freenote.repository.UserRepository;
import be.freenote.security.JwtTokenProvider;
import be.freenote.service.impl.AuthServiceImpl;
import be.freenote.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private JavaMailSender mailSender;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private MimeMessage mimeMessage;

    @InjectMocks private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "emailHashSalt", "test-salt");
    }

    // ---- processOAuth2Login ----

    @Test
    void shouldReturnJwtWhenOAuth2LoginWithExistingUser() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("oauth-id-123");

        User existingUser = User.builder().id(1L).oauthProvider("GOOGLE").oauthId("oauth-id-123")
                .username("test").build();
        when(userRepository.findByOauthProviderAndOauthId("GOOGLE", "oauth-id-123"))
                .thenReturn(Optional.of(existingUser));
        when(jwtTokenProvider.generateToken(existingUser)).thenReturn("jwt-token");

        String jwt = authService.processOAuth2Login(oAuth2User, "google");

        assertThat(jwt).isEqualTo("jwt-token");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldCreateUserAndReturnJwtWhenOAuth2LoginWithNewUser() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("new-id");
        when(oAuth2User.getAttribute("name")).thenReturn("John Doe");

        when(userRepository.findByOauthProviderAndOauthId("DISCORD", "new-id"))
                .thenReturn(Optional.empty());
        User savedUser = User.builder().id(2L).oauthProvider("DISCORD").oauthId("new-id")
                .username("John Doe").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("new-jwt");

        String jwt = authService.processOAuth2Login(oAuth2User, "discord");

        assertThat(jwt).isEqualTo("new-jwt");
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void shouldFallbackToUsernameAttributeWhenNameIsNull() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("oid");
        when(oAuth2User.getAttribute("name")).thenReturn(null);
        when(oAuth2User.getAttribute("username")).thenReturn("discord_user");

        when(userRepository.findByOauthProviderAndOauthId("DISCORD", "oid")).thenReturn(Optional.empty());
        User saved = User.builder().id(3L).username("discord_user").build();
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt");

        authService.processOAuth2Login(oAuth2User, "discord");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().getFirst().getUsername()).isEqualTo("discord_user");
    }

    @Test
    void shouldFallbackToOauthIdWhenAllAttributesNull() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("fallback-id");
        when(oAuth2User.getAttribute("name")).thenReturn(null);
        when(oAuth2User.getAttribute("username")).thenReturn(null);

        when(userRepository.findByOauthProviderAndOauthId("GOOGLE", "fallback-id")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt");

        authService.processOAuth2Login(oAuth2User, "google");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().getFirst().getUsername()).isEqualTo("fallback-id");
    }

    @Test
    void shouldUppercaseRegistrationId() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("id1");

        User user = User.builder().id(1L).username("u").build();
        when(userRepository.findByOauthProviderAndOauthId("GOOGLE", "id1"))
                .thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt");

        authService.processOAuth2Login(oAuth2User, "Google");

        verify(userRepository).findByOauthProviderAndOauthId("GOOGLE", "id1");
    }

    // ---- requestVerification ----

    @Test
    void shouldSendCodeWhenValidIsfceEmail() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(userRepository.findByEmailHash(anyString())).thenReturn(Optional.empty());
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        authService.requestVerification(1L, "student@isfce.be");

        verify(valueOps).set(eq("verify:1"), anyString(), eq(Duration.ofMinutes(15)));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldStoreCodeAndEmailHashInRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(userRepository.findByEmailHash(anyString())).thenReturn(Optional.empty());
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        authService.requestVerification(42L, "test@isfce.be");

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOps).set(eq("verify:42"), valueCaptor.capture(), eq(Duration.ofMinutes(15)));

        String storedValue = valueCaptor.getValue();
        assertThat(storedValue).contains(":");
        String[] parts = storedValue.split(":", 2);
        assertThat(parts[0]).matches("\\d{6}"); // 6-digit code
        assertThat(parts[1]).isEqualTo(HashUtil.hashEmail("test@isfce.be", "test-salt"));
    }

    @Test
    void shouldThrowWhenInvalidEmail() {
        assertThatThrownBy(() -> authService.requestVerification(1L, "user@gmail.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISFCE");
    }

    @Test
    void shouldThrowWhenSubdomainEmail() {
        assertThatThrownBy(() -> authService.requestVerification(1L, "user@sub.isfce.be"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAcceptUppercaseIsfceEmail() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(userRepository.findByEmailHash(anyString())).thenReturn(Optional.empty());
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThatCode(() -> authService.requestVerification(1L, "Student@ISFCE.BE"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowDuplicateWhenEmailHashAlreadyExists() {
        User existing = User.builder().id(99L).build();
        when(userRepository.findByEmailHash(anyString())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> authService.requestVerification(1L, "student@isfce.be"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already verified");
    }

    // ---- confirmVerification ----

    @Test
    void shouldVerifyUserWhenCorrectCode() {
        String emailHash = HashUtil.hashEmail("test@isfce.be", "test-salt");
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("verify-attempts:1")).thenReturn(1L);
        when(valueOps.get("verify:1")).thenReturn("123456:" + emailHash);

        User user = User.builder().id(1L).username("test").verified(false).role("USER").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(jwtTokenProvider.generateToken(user)).thenReturn("verified-jwt");

        String jwt = authService.confirmVerification(1L, "123456");

        assertThat(jwt).isEqualTo("verified-jwt");
        assertThat(user.isVerified()).isTrue();
        assertThat(user.getEmailHash()).isEqualTo(emailHash);
        verify(redisTemplate).delete("verify:1");
        verify(redisTemplate).delete("verify-attempts:1");
    }

    @Test
    void shouldThrowWhenIncorrectCode() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("verify-attempts:1")).thenReturn(1L);
        when(valueOps.get("verify:1")).thenReturn("123456:somehash");

        assertThatThrownBy(() -> authService.confirmVerification(1L, "000000"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void shouldThrowWhenCodeExpired() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("verify-attempts:1")).thenReturn(1L);
        when(valueOps.get("verify:1")).thenReturn(null);

        assertThatThrownBy(() -> authService.confirmVerification(1L, "123456"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void shouldBlockAfter5FailedAttempts() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("verify-attempts:1")).thenReturn(6L);

        assertThatThrownBy(() -> authService.confirmVerification(1L, "000000"))
                .isInstanceOf(RateLimitExceededException.class);

        verify(redisTemplate).delete("verify:1");
        verify(redisTemplate).delete("verify-attempts:1");
    }

    @Test
    void shouldSetExpireOnFirstAttemptOnly() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("verify-attempts:1")).thenReturn(1L);
        when(valueOps.get("verify:1")).thenReturn("123456:hash");

        User user = User.builder().id(1L).username("t").verified(false).role("USER").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt");

        authService.confirmVerification(1L, "123456");

        verify(redisTemplate).expire("verify-attempts:1", Duration.ofMinutes(15));
    }

    @Test
    void shouldNotSetExpireOnSubsequentAttempts() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("verify-attempts:1")).thenReturn(3L);
        when(valueOps.get("verify:1")).thenReturn("123456:hash");

        User user = User.builder().id(1L).username("t").verified(false).role("USER").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt");

        authService.confirmVerification(1L, "123456");

        verify(redisTemplate, never()).expire(eq("verify-attempts:1"), any(Duration.class));
    }
}

package be.notarium.service.impl;

import be.notarium.entity.User;
import be.notarium.entity.UserProfile;
import be.notarium.exception.DuplicateResourceException;
import be.notarium.exception.RateLimitExceededException;
import be.notarium.exception.ResourceNotFoundException;
import be.notarium.exception.ServiceUnavailableException;
import be.notarium.exception.UnauthorizedException;
import be.notarium.repository.UserRepository;
import be.notarium.security.JwtTokenProvider;
import be.notarium.service.AuthService;
import be.notarium.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Pattern ISFCE_EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@isfce\\.be$", Pattern.CASE_INSENSITIVE);

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${app.email.hash-salt}")
    private String emailHashSalt;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String processOAuth2Login(OAuth2User oAuth2User, String registrationId) {
        String provider = registrationId.toUpperCase();
        String oauthId = oAuth2User.getName();

        User user = userRepository.findByOauthProviderAndOauthId(provider, oauthId)
                .orElseGet(() -> {
                    String displayName = oAuth2User.getAttribute("name");
                    if (displayName == null) {
                        displayName = oAuth2User.getAttribute("username");
                    }
                    if (displayName == null) {
                        displayName = oauthId;
                    }

                    User newUser = User.builder()
                            .oauthProvider(provider)
                            .oauthId(oauthId)
                            .username(displayName)
                            .build();
                    User saved = userRepository.save(newUser);
                    UserProfile profile = UserProfile.builder().user(saved).build();
                    saved.setProfile(profile);
                    return userRepository.save(saved);
                });

        return jwtTokenProvider.generateToken(user);
    }

    @Override
    @Transactional(readOnly = true)
    public void requestVerification(Long userId, String email) {
        if (!ISFCE_EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email must be an ISFCE email address (@isfce.be)");
        }

        String emailHash = HashUtil.hashEmail(email, emailHashSalt);

        if (userRepository.findByEmailHash(emailHash).isPresent()) {
            throw new DuplicateResourceException("This email is already verified by another account");
        }

        String code = generateCode();
        redisTemplate.opsForValue().set("verify:" + userId, code + ":" + emailHash, Duration.ofMinutes(15));

        sendVerificationEmail(email, code);
    }

    @Override
    @Transactional
    public String confirmVerification(Long userId, String code) {
        String attemptsKey = "verify-attempts:" + userId;
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptsKey, Duration.ofMinutes(15));
        }
        if (attempts != null && attempts > 5) {
            redisTemplate.delete("verify:" + userId);
            redisTemplate.delete(attemptsKey);
            throw new RateLimitExceededException("Trop de tentatives, veuillez redemander un code");
        }

        String redisKey = "verify:" + userId;
        String storedValue = redisTemplate.opsForValue().get(redisKey);

        if (storedValue == null) {
            throw new UnauthorizedException("Verification code expired or not found");
        }

        String[] parts = storedValue.split(":", 2);
        String storedCode = parts[0];
        String emailHash = parts[1];

        if (!storedCode.equals(code)) {
            throw new UnauthorizedException("Invalid verification code");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setEmailHash(emailHash);
        user.setVerified(true);
        userRepository.save(user);

        redisTemplate.delete(redisKey);
        redisTemplate.delete(attemptsKey);

        return jwtTokenProvider.generateToken(user);
    }

    private String generateCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendVerificationEmail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Notarium - Code de vérification");
            helper.setText(
                    """
                    <html>
                    <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                        <h2 style="color: #2563eb;">Notarium</h2>
                        <p>Votre code de vérification est :</p>
                        <div style="background: #f1f5f9; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;">
                            <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #1e293b;">%s</span>
                        </div>
                        <p style="color: #64748b; font-size: 14px;">Ce code expire dans 15 minutes.</p>
                    </body>
                    </html>
                    """.formatted(code),
                    true
            );
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new ServiceUnavailableException("Échec de l'envoi de l'email de vérification", e);
        }
    }
}

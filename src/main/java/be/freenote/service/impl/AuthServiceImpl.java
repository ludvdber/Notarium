package be.freenote.service.impl;

import be.freenote.dto.response.LinkedProviderResponse;
import be.freenote.entity.User;
import be.freenote.entity.UserOauthLink;
import be.freenote.entity.UserProfile;
import be.freenote.enums.AvatarSource;
import be.freenote.exception.DuplicateResourceException;
import be.freenote.exception.ForbiddenException;
import be.freenote.exception.RateLimitExceededException;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.exception.ServiceUnavailableException;
import be.freenote.exception.UnauthorizedException;
import be.freenote.repository.BanRepository;
import be.freenote.repository.Repositories;
import be.freenote.repository.UserOauthLinkRepository;
import be.freenote.repository.UserRepository;
import be.freenote.security.JwtTokenProvider;
import be.freenote.service.AuthService;
import be.freenote.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Pattern ISFCE_EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@isfce\\.be$", Pattern.CASE_INSENSITIVE);

    private final UserRepository userRepository;
    private final UserOauthLinkRepository oauthLinkRepository;
    private final BanRepository banRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${app.email.hash-salt}")
    private String emailHashSalt;

    // Expéditeur des mails transactionnels. DOIT correspondre au domaine authentifié chez le
    // fournisseur SMTP (Brevo) — sinon SPF/DKIM échouent et le mail tombe en spam.
    @Value("${app.email.from:noreply@freenote.be}")
    private String mailFrom;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void processOAuth2Login(OAuth2User oAuth2User, String registrationId) {
        String provider = registrationId.toUpperCase();
        String oauthId = oAuth2User.getName();

        Boolean emailVerified = oAuth2User.getAttribute("email_verified");
        if (Boolean.FALSE.equals(emailVerified)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unverified_email", "OAuth provider email is not verified", null));
        }

        // Banned Discord identity: refuse login outright (cannot re-create an account).
        if (banRepository.existsByOauthProviderAndOauthId(provider, oauthId)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_banned", "This account has been banned", null));
        }

        // Find the linked account, or provision a new one. The JWT cookie is minted later by
        // OAuth2LoginSuccessHandler — this method only ensures the account exists.
        oauthLinkRepository.findByProviderAndOauthId(provider, oauthId)
                .map(UserOauthLink::getUser)
                .orElseGet(() -> createUserFromOAuth(oAuth2User, provider, oauthId));
    }

    /** Adds a (provider, oauthId) link to an already-authenticated user. */
    @Override
    @Transactional
    public void linkProvider(Long currentUserId, OAuth2User oAuth2User, String registrationId) {
        String provider = registrationId.toUpperCase();
        String oauthId = oAuth2User.getName();

        oauthLinkRepository.findByProviderAndOauthId(provider, oauthId).ifPresent(existing -> {
            if (!existing.getUser().getId().equals(currentUserId)) {
                // Refuse to silently transfer the link from another user — that would let an attacker
                // who controls a victim's OAuth account hijack their Freenote account.
                throw new DuplicateResourceException(
                        "Ce compte " + provider + " est déjà lié à un autre utilisateur Freenote");
            }
        });

        // Idempotent: if already linked to the same user, just refresh the avatar.
        UserOauthLink link = oauthLinkRepository.findByUserIdAndProvider(currentUserId, provider)
                .orElseGet(() -> {
                    User user = Repositories.findByIdOrThrow(userRepository, currentUserId, "User");
                    UserOauthLink fresh = UserOauthLink.builder()
                            .user(user)
                            .provider(provider)
                            .oauthId(oauthId)
                            .build();
                    return oauthLinkRepository.save(fresh);
                });
        // If the provider returns a different oauthId (re-link after deleting at provider), update it.
        link.setOauthId(oauthId);

        log.info("Linked provider {} to userId={}", provider, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkedProviderResponse> getLinkedProviders(Long userId) {
        return oauthLinkRepository.findByUserId(userId).stream()
                .map(l -> new LinkedProviderResponse(l.getProvider(), l.getLinkedAt()))
                .toList();
    }

    @Override
    @Transactional
    public void unlinkProvider(Long userId, String provider) {
        String up = provider.toUpperCase();
        UserOauthLink link = oauthLinkRepository.findByUserIdAndProvider(userId, up)
                .orElseThrow(() -> new ResourceNotFoundException("OAuth link", "provider", up));
        if (oauthLinkRepository.countByUserId(userId) <= 1) {
            // Removing the last provider would lock the user out — refuse.
            throw new DuplicateResourceException(
                    "Tu ne peux pas délier ton dernier moyen de connexion");
        }
        oauthLinkRepository.delete(link);
        log.info("Unlinked provider {} from userId={}", up, userId);
    }

    private User createUserFromOAuth(OAuth2User oAuth2User, String provider, String oauthId) {
        // Provisional account: the username is NOT derived from Discord — the user picks it during
        // onboarding (usernameChosen=false gates the app until then). Discord only provides auth +
        // the profile picture. A throwaway placeholder keeps the NOT NULL/unique constraint happy.
        User newUser = User.builder()
                .username(uniqueUsername("membre-" + UUID.randomUUID().toString().substring(0, 8)))
                .usernameChosen(false)
                .build();
        User saved = userRepository.save(newUser);

        UserProfile profile = UserProfile.builder()
                .user(saved)
                .avatarSource(AvatarSource.DISCORD)
                .discordAvatarUrl(discordAvatarUrl(oAuth2User, oauthId))
                .build();
        saved.setProfile(profile);

        UserOauthLink link = UserOauthLink.builder()
                .user(saved)
                .provider(provider)
                .oauthId(oauthId)
                .build();
        oauthLinkRepository.save(link);

        return userRepository.save(saved);
    }

    /** Appends a numeric suffix until the username is free (placeholder collisions are astronomically rare). */
    private String uniqueUsername(String base) {
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    /** Builds the Discord CDN avatar URL from the OAuth attributes, or null if the user has no custom avatar. */
    private static String discordAvatarUrl(OAuth2User oAuth2User, String oauthId) {
        Object avatar = oAuth2User.getAttribute("avatar");
        if (avatar == null) return null;
        return "https://cdn.discordapp.com/avatars/" + oauthId + "/" + avatar + ".png";
    }

    @Override
    public void requestVerification(Long userId, String email) {
        if (!ISFCE_EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email must be an ISFCE email address (@isfce.be)");
        }

        String emailHash = HashUtil.hashEmail(email, emailHashSalt);

        // Banned ISFCE email: refuse verification. The user is told (it's their own address, so no
        // enumeration of other accounts) — the only way back is a brand-new @isfce.be address.
        if (banRepository.existsByEmailHash(emailHash)) {
            throw new ForbiddenException("Cette adresse @isfce.be a été bannie");
        }

        // Silently no-op if this email is already claimed by another account.
        // Returning a distinct error would let an attacker enumerate registered @isfce.be emails.
        if (userRepository.findByEmailHash(emailHash).isPresent()) {
            log.info("Verification request for an already-claimed email (userId={}). Silently ignored.", userId);
            return;
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
            log.warn("Verification rate limit reached for userId={}", userId);
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

        // Defence in depth: a ban could have been issued between request and confirm.
        if (banRepository.existsByEmailHash(emailHash)) {
            redisTemplate.delete(redisKey);
            throw new ForbiddenException("Cette adresse @isfce.be a été bannie");
        }

        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");

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
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject("Freenote - Code de vérification");
            helper.setText(
                    """
                    <html>
                    <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                        <h2 style="color: #2563eb;">Freenote</h2>
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
        } catch (MessagingException | org.springframework.mail.MailException e) {
            // MessagingException (checked, from MimeMessageHelper) and MailException (unchecked,
            // from JavaMailSender.send — e.g. SMTP unreachable) both mean "couldn't send". Surface
            // a clean 503 instead of letting MailException bubble up as a 500 + stack trace.
            log.warn("Verification email could not be sent to a user: {}", e.getMessage());
            throw new ServiceUnavailableException("Échec de l'envoi de l'email de vérification", e);
        }
    }
}

package be.freenote.mapper;

import be.freenote.dto.response.LeaderboardEntry;
import be.freenote.dto.response.ProfileCardResponse;
import be.freenote.dto.response.UserResponse;
import be.freenote.entity.Badge;
import be.freenote.entity.User;
import be.freenote.entity.UserProfile;
import be.freenote.repository.DocumentRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected DocumentRepository documentRepository;

    public UserResponse toResponse(User user) {
        UserProfile p = user.getProfile();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getXp(),
                p != null ? p.getBio() : null,
                p != null ? p.getWebsite() : null,
                p != null ? p.getGithub() : null,
                p != null ? p.getLinkedin() : null,
                p != null ? p.getDiscord() : null,
                mapBadges(user.getBadges()),
                documentRepository.countByUserId(user.getId()),
                p != null && p.isProfilePublic(),
                p != null && p.isAdFree(),
                p != null && p.getTermsAcceptedAt() != null
        );
    }

    /**
     * Returns a filtered response when the profile is not public.
     * Hides bio and social links; keeps username, XP, badges, doc count.
     */
    public UserResponse toPublicResponse(User user) {
        UserProfile p = user.getProfile();
        if (p != null && p.isProfilePublic()) {
            return toResponse(user);
        }
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getXp(),
                null, null, null, null, null,
                mapBadges(user.getBadges()),
                documentRepository.countByUserId(user.getId()),
                false,
                p != null && p.isAdFree(),
                p != null && p.getTermsAcceptedAt() != null
        );
    }

    public LeaderboardEntry toLeaderboardEntry(User user, int rank) {
        UserProfile p = user.getProfile();
        return new LeaderboardEntry(
                rank,
                user.getUsername(),
                user.getXp(),
                documentRepository.countByUserId(user.getId()),
                mapBadges(user.getBadges()),
                p != null && p.isAdFree()
        );
    }

    public ProfileCardResponse toProfileCard(User user) {
        UserProfile p = user.getProfile();
        return new ProfileCardResponse(
                user.getUsername(),
                user.getRole(),
                p != null ? p.getDiscord() : null,
                p != null ? p.getGithub() : null,
                p != null ? p.getLinkedin() : null,
                mapBadges(user.getBadges()),
                p != null && p.isAdFree()
        );
    }

    protected List<String> mapBadges(List<Badge> badges) {
        if (badges == null) {
            return List.of();
        }
        return badges.stream().map(Badge::getBadgeType).toList();
    }
}

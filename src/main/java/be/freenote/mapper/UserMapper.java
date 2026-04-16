package be.freenote.mapper;

import be.freenote.dto.response.LeaderboardEntry;
import be.freenote.dto.response.ProfileCardResponse;
import be.freenote.dto.response.UserResponse;
import be.freenote.entity.Badge;
import be.freenote.entity.User;
import be.freenote.entity.UserProfile;
import org.mapstruct.*;

import java.util.List;

/**
 * Pure mapper — no repository injection, no queries.
 * The calling service is responsible for providing computed values like documentCount.
 */
@Mapper(componentModel = "spring")
public abstract class UserMapper {

    public UserResponse toResponse(User user, long documentCount) {
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
                documentCount,
                p != null && p.isProfilePublic(),
                p != null && p.isAdFree(),
                p != null && p.getTermsAcceptedAt() != null
        );
    }

    public UserResponse toPublicResponse(User user, long documentCount) {
        UserProfile p = user.getProfile();
        if (p != null && p.isProfilePublic()) {
            return toResponse(user, documentCount);
        }
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getXp(),
                null, null, null, null, null,
                mapBadges(user.getBadges()),
                documentCount,
                false,
                p != null && p.isAdFree(),
                p != null && p.getTermsAcceptedAt() != null
        );
    }

    public LeaderboardEntry toLeaderboardEntry(User user, int rank, long documentCount) {
        UserProfile p = user.getProfile();
        return new LeaderboardEntry(
                user.getId(),
                rank,
                user.getUsername(),
                user.getXp(),
                documentCount,
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

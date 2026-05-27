package be.freenote.dto.response;

public record UserResponse(
        Long id,
        String username,
        String role,
        boolean verified,
        int xp,
        String bio,
        String website,
        String github,
        String linkedin,
        String discord,
        long documentCount,
        boolean profilePublic,
        boolean showInCarousel,
        boolean supporter,
        boolean termsAccepted,
        String avatarUrl,
        String avatarSource,
        String displayName,
        String firstName,
        String lastName,
        boolean displayRealName
) {}

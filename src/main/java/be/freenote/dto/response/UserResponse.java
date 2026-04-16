package be.freenote.dto.response;

import java.util.List;

public record UserResponse(
        Long id,
        String username,
        int xp,
        String bio,
        String website,
        String github,
        String linkedin,
        String discord,
        List<String> badges,
        long documentCount,
        boolean profilePublic,
        boolean supporter,
        boolean termsAccepted
) {}

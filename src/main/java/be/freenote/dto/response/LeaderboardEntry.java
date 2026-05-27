package be.freenote.dto.response;

public record LeaderboardEntry(
        Long userId,
        int rank,
        String username,
        String displayName,
        int xp,
        long documentCount,
        boolean supporter,
        String avatarUrl
) {}

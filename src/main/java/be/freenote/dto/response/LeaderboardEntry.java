package be.freenote.dto.response;

import java.util.List;

public record LeaderboardEntry(
        int rank,
        String username,
        int xp,
        long documentCount,
        List<String> badges,
        boolean supporter
) {}

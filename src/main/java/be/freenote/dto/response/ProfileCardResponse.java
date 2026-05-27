package be.freenote.dto.response;

public record ProfileCardResponse(
        String username,
        String displayName,
        String role,
        String discord,
        String github,
        String linkedin,
        boolean supporter,
        String avatarUrl
) {}

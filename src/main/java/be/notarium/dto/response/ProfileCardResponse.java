package be.notarium.dto.response;

import java.util.List;

public record ProfileCardResponse(
        String username,
        String role,
        String discord,
        String github,
        String linkedin,
        List<String> badges,
        boolean supporter
) {}

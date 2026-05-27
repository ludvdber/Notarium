package be.freenote.dto.response;

import java.time.LocalDateTime;

public record LinkedProviderResponse(
        String provider,
        LocalDateTime linkedAt
) {}

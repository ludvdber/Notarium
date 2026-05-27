package be.freenote.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificationResponse(
        Long id,
        String type,
        Map<String, Object> payload,
        boolean read,
        LocalDateTime createdAt
) {}

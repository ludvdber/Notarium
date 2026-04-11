package be.notarium.dto.response;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        Long documentId,
        String documentTitle,
        String reporterUsername,
        String reason,
        String status,
        LocalDateTime createdAt
) {}

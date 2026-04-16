package be.freenote.dto.response;

import java.time.LocalDate;

public record DelegateHistoryResponse(
        Long id,
        String sectionName,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {}

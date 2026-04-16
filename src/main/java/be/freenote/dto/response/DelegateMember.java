package be.freenote.dto.response;

import java.time.LocalDate;

public record DelegateMember(
        Long id,
        Long userId,
        String displayName,
        String username,
        String discord,
        LocalDate startDate,
        LocalDate endDate
) {}

package be.freenote.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DonationResponse(
        Long id,
        Long userId,
        String username,
        BigDecimal amount,
        String kofiTransactionId,
        LocalDateTime adFreeUntil
) {}

package be.freenote.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EndDelegateRequest {

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}

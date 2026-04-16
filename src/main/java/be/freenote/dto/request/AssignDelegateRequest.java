package be.freenote.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignDelegateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Section ID is required")
    private Long sectionId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
}

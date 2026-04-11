package be.notarium.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmCodeRequest {

    @NotBlank(message = "Code is required")
    private String code;
}

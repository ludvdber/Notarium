package be.freenote.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProfessorRequest {

    @NotBlank(message = "Name is required")
    private String name;
}

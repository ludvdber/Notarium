package be.notarium.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    private String website;

    private String github;

    private String linkedin;

    private String discord;

    private boolean profilePublic;

    private boolean showInCarousel;

    private String themePref;
}

package be.notarium.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateDocumentRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Category is required")
    private String category;

    private String year;

    private Long professorId;

    @NotBlank(message = "Language is required")
    private String language;

    private boolean aiGenerated;

    private boolean anonymous;

    private List<String> tags;
}

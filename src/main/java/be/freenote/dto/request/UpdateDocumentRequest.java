package be.freenote.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateDocumentRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String category;

    private String language;

    @Size(max = 20, message = "Year must not exceed 20 characters")
    private String year;

    private Long professorId;

    private Boolean verified;

    private List<String> tags;
}

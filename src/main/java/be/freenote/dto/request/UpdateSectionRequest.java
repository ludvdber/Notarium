package be.freenote.dto.request;

import lombok.Data;

@Data
public class UpdateSectionRequest {
    /** Null clears the user's section. */
    private Long sectionId;
}

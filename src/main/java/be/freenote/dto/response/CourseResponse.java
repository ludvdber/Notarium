package be.freenote.dto.response;

public record CourseResponse(
        Long id,
        String name,
        Long sectionId,
        String sectionName,
        long documentCount,
        boolean approved
) {}

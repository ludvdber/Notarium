package be.freenote.dto.response;

public record CourseResponse(
        Long id,
        String name,
        String sectionName,
        long documentCount
) {}

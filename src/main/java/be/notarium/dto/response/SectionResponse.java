package be.notarium.dto.response;

public record SectionResponse(
        Long id,
        String name,
        String icon,
        long documentCount
) {}

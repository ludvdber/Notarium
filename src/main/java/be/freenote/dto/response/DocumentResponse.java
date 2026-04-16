package be.freenote.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentResponse(
        Long id,
        String title,
        String courseName,
        String sectionName,
        String category,
        String authorName,
        boolean verified,
        boolean aiGenerated,
        String language,
        String year,
        String professorName,
        double averageRating,
        int downloadCount,
        List<String> tags,
        String summaryAi,
        LocalDateTime createdAt
) {}

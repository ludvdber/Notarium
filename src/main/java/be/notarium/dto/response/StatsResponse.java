package be.notarium.dto.response;

public record StatsResponse(
        long totalDocs,
        long totalDownloads,
        long totalContributors,
        long totalCourses,
        long weekUploads
) {}

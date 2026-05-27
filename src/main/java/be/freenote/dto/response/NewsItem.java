package be.freenote.dto.response;

import java.util.List;

public record NewsItem(
        String title,
        String date,
        List<String> labels,
        String url
) {}

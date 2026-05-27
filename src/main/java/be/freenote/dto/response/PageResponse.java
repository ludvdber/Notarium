package be.freenote.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    /**
     * Builds a response from a Spring Data {@link Page} and the already-mapped content list.
     * The content is passed separately because the caller usually maps entities → DTOs.
     */
    public static <T> PageResponse<T> from(Page<?> page, List<T> content) {
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}

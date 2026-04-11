package be.notarium.service;

import be.notarium.dto.response.DocumentResponse;
import be.notarium.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface FavoriteService {
    boolean toggle(Long userId, Long documentId);
    PageResponse<DocumentResponse> getFavorites(Long userId, Pageable pageable);
}

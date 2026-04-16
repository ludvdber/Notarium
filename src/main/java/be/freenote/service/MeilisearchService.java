package be.freenote.service;

import be.freenote.entity.Document;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MeilisearchService {
    void indexDocument(Document document);
    List<Long> search(String query, Long courseId, String category, String sort, Pageable pageable);
    void deleteDocument(Long documentId);
}

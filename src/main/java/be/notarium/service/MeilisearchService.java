package be.notarium.service;

import be.notarium.entity.Document;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface MeilisearchService {
    void indexDocument(Document document);
    List<Long> search(String query, Long courseId, String category, String sort, Pageable pageable);
    void deleteDocument(Long documentId);
}

package be.notarium.service;

import be.notarium.dto.request.CreateDocumentRequest;
import be.notarium.dto.response.DocumentResponse;
import be.notarium.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentResponse create(CreateDocumentRequest request, MultipartFile file, Long userId);
    DocumentResponse getById(Long id);
    PageResponse<DocumentResponse> search(String query, Long courseId, String category, String sort, Pageable pageable);
    void delete(Long documentId, Long userId);
    List<DocumentResponse> getPopular();
    List<DocumentResponse> getRecent();
    List<DocumentResponse> getUnverified();
    DocumentResponse verify(Long documentId);
    byte[] download(Long documentId, Long userId);
}

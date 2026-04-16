package be.freenote.controller;

import be.freenote.dto.request.CreateDocumentRequest;
import be.freenote.dto.response.DocumentResponse;
import be.freenote.dto.response.PageResponse;
import be.freenote.security.ratelimit.RateLimit;
import be.freenote.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document upload, search and management")
public class DocumentController {

    private final DocumentService documentService;
    private final be.freenote.repository.TagRepository tagRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RateLimit(max = 5, window = 86400)
    @Operation(summary = "Upload a document",
               description = "Uploads a PDF document with metadata. Requires verified role. Max 10 MB.")
    public ResponseEntity<DocumentResponse> create(Authentication authentication,
                                                    @Valid @RequestPart("data") CreateDocumentRequest request,
                                                    @RequestPart("file") MultipartFile file) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.create(request, file, userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getById(id));
    }

    @GetMapping("/{id}/file")
    @Operation(summary = "Download document PDF",
               description = "Streams the PDF file. Buffers the download count via Redis and flushes to DB every 5 minutes.")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id, Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        byte[] data = documentService.download(id, userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"document.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/search")
    @Operation(summary = "Search documents")
    public ResponseEntity<PageResponse<DocumentResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(documentService.search(q, courseId, category, sort,
                PageRequest.of(page, size)));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular documents")
    public ResponseEntity<List<DocumentResponse>> getPopular() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(2)).cachePublic())
                .body(documentService.getPopular());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get documents by user ID")
    public ResponseEntity<PageResponse<DocumentResponse>> getByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return ResponseEntity.ok(documentService.getByUser(userId, PageRequest.of(page, size)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        documentService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all distinct tag labels for autocomplete")
    public ResponseEntity<List<String>> getTags() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(tagRepository.findDistinctLabels());
    }
}

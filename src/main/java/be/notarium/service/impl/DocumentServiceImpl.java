package be.notarium.service.impl;

import be.notarium.dto.request.CreateDocumentRequest;
import be.notarium.dto.response.DocumentResponse;
import be.notarium.dto.response.PageResponse;
import be.notarium.entity.*;
import be.notarium.enums.Category;
import be.notarium.exception.ForbiddenException;
import be.notarium.exception.PayloadTooLargeException;
import be.notarium.exception.ResourceNotFoundException;
import be.notarium.mapper.DocumentMapper;
import be.notarium.repository.*;
import be.notarium.service.DocumentService;
import be.notarium.service.MeilisearchService;
import be.notarium.service.MinioService;
import be.notarium.service.PdfCompressionService;
import be.notarium.service.UserService;
import be.notarium.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46, 0x2D}; // %PDF-
    private static final String DL_BUFFER_PREFIX = "dl-buffer:";

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ProfessorRepository professorRepository;
    private final DocumentMapper documentMapper;
    private final MinioService minioService;
    private final PdfCompressionService pdfCompressionService;
    private final MeilisearchService meilisearchService;
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public DocumentResponse create(CreateDocumentRequest request, MultipartFile file, Long userId) {
        if (!PDF_CONTENT_TYPE.equals(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are accepted");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new PayloadTooLargeException("File size exceeds the 10 MB limit");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));

        Professor professor = null;
        if (request.getProfessorId() != null) {
            professor = professorRepository.findById(request.getProfessorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", request.getProfessorId()));
        }

        byte[] pdfBytes;
        try {
            pdfBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        if (pdfBytes.length < 5 || !java.util.Arrays.equals(PDF_MAGIC, 0, 5, pdfBytes, 0, 5)) {
            throw new IllegalArgumentException("Le fichier n'est pas un PDF valide");
        }

        // Validate category against enum
        Category category;
        try {
            category = Category.valueOf(request.getCategory());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + request.getCategory());
        }

        byte[] compressed = pdfCompressionService.compress(pdfBytes);

        String fileKey = UUID.randomUUID() + "/" + FileUtil.sanitizeFileName(file.getOriginalFilename());
        minioService.upload(fileKey, new ByteArrayInputStream(compressed), compressed.length, PDF_CONTENT_TYPE);

        Document document = Document.builder()
                .title(request.getTitle())
                .course(course)
                .category(category)
                .fileKey(fileKey)
                .user(user)
                .anonymous(request.isAnonymous())
                .language(request.getLanguage())
                .aiGenerated(request.isAiGenerated())
                .year(request.getYear())
                .professor(professor)
                .fileSize((long) compressed.length)
                .build();

        Document saved = documentRepository.save(document);

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            request.getTags().forEach(label -> {
                Tag tag = Tag.builder().document(saved).label(label).build();
                saved.getTags().add(tag);
            });
            documentRepository.save(saved);
        }

        meilisearchService.indexDocument(saved);
        userService.addXp(userId, 10);

        return documentMapper.toResponse(saved);
    }

    @Override
    public DocumentResponse getById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        return documentMapper.toResponse(document);
    }

    @Override
    public PageResponse<DocumentResponse> search(String query, Long courseId, String category,
                                                   String sort, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            List<Long> ids = meilisearchService.search(query, courseId, category, sort, pageable);
            if (!ids.isEmpty()) {
                List<DocumentResponse> results = documentRepository.findAllById(ids).stream()
                        .map(documentMapper::toResponse)
                        .toList();
                return new PageResponse<>(results, pageable.getPageNumber(), pageable.getPageSize(),
                        results.size(), 1);
            }
        }

        Page<Document> page;
        if (courseId != null && category != null) {
            page = documentRepository.findByCourseIdAndCategory(courseId, Category.valueOf(category), pageable);
        } else {
            page = documentRepository.findAll(pageable);
        }

        List<DocumentResponse> content = page.getContent().stream()
                .map(documentMapper::toResponse)
                .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional
    public void delete(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        boolean isAuthor = document.getUser() != null && document.getUser().getId().equals(userId);
        boolean isAdmin = "ADMIN".equals(user.getRole());
        if (!isAuthor && !isAdmin) {
            throw new ForbiddenException("You can only delete your own documents");
        }

        minioService.delete(document.getFileKey());
        meilisearchService.deleteDocument(document.getId());
        documentRepository.delete(document);
    }

    @Override
    public List<DocumentResponse> getPopular() {
        return documentRepository.findTop10ByOrderByDownloadCountDesc().stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    public List<DocumentResponse> getRecent() {
        return documentRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    public List<DocumentResponse> getUnverified() {
        return documentRepository.findByVerifiedFalse().stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DocumentResponse verify(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        document.setVerified(true);
        return documentMapper.toResponse(documentRepository.save(document));
    }

    // --- Download with Redis buffer ---

    @Override
    public byte[] download(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        // Buffer in Redis — no DB write on each download
        redisTemplate.opsForValue().increment(DL_BUFFER_PREFIX + documentId);

        // Award 1 XP to author (cheap Redis-backed operation in UserService)
        if (document.getUser() != null) {
            userService.addXp(document.getUser().getId(), 1);
        }

        return minioService.download(document.getFileKey());
    }

    /** Flush buffered download counts to DB every 5 minutes */
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void flushDownloadCounts() {
        Set<String> keys = redisTemplate.keys(DL_BUFFER_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            String value = redisTemplate.opsForValue().getAndDelete(key);
            if (value == null) continue;

            long increment = Long.parseLong(value);
            Long docId = Long.parseLong(key.substring(DL_BUFFER_PREFIX.length()));

            documentRepository.incrementDownloadCount(docId, (int) increment);
        }
    }
}

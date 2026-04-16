package be.freenote.service.impl;

import be.freenote.dto.request.CreateDocumentRequest;
import be.freenote.dto.request.UpdateDocumentRequest;
import be.freenote.dto.response.DocumentResponse;
import be.freenote.dto.response.PageResponse;
import be.freenote.entity.*;
import be.freenote.enums.Category;
import be.freenote.exception.ForbiddenException;
import be.freenote.exception.PayloadTooLargeException;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.mapper.DocumentMapper;
import be.freenote.repository.*;
import be.freenote.service.DocumentService;
import be.freenote.service.MeilisearchService;
import be.freenote.service.MinioService;
import be.freenote.service.PdfCompressionService;
import be.freenote.service.UserService;
import be.freenote.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46, 0x2D}; // %PDF-
    private static final String DL_BUFFER_PREFIX = "dl-buffer:";

    private static String sanitize(String input) {
        if (input == null) return null;
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

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
                .title(sanitize(request.getTitle()))
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
                Tag tag = Tag.builder().document(saved).label(sanitize(label.strip().toLowerCase())).build();
                saved.getTags().add(tag);
            });
            documentRepository.save(saved);
        }

        meilisearchService.indexDocument(saved); // async — does not block the transaction
        // XP is awarded when admin verifies the document, not at upload — prevents spam farming

        log.info("Document uploaded: id={}, title='{}', user={}, size={}KB",
                saved.getId(), saved.getTitle(), userId, compressed.length / 1024);

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
            page = documentRepository.findByVerifiedTrueAndCourseIdAndCategory(courseId, Category.valueOf(category), pageable);
        } else if (courseId != null) {
            page = documentRepository.findByVerifiedTrueAndCourseId(courseId, pageable);
        } else if (category != null) {
            page = documentRepository.findByVerifiedTrueAndCategory(Category.valueOf(category), pageable);
        } else {
            page = documentRepository.findByVerifiedTrue(pageable);
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
        log.info("Document deleted: id={}, by user={}", documentId, userId);
    }

    @Override
    public List<DocumentResponse> getPopular() {
        return documentRepository.findTop10ByVerifiedTrueOrderByDownloadCountDesc().stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    public List<DocumentResponse> getRecent() {
        return documentRepository.findTop10ByVerifiedTrueOrderByCreatedAtDesc().stream()
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
        Document saved = documentRepository.save(document);

        // Award XP to author on verification (not at upload) — prevents spam farming
        if (document.getUser() != null) {
            userService.addXp(document.getUser().getId(), 10);
        }

        return documentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DocumentResponse adminUpdate(Long documentId, UpdateDocumentRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            document.setTitle(sanitize(request.getTitle()));
        }
        if (request.getCategory() != null) {
            document.setCategory(Category.valueOf(request.getCategory()));
        }
        if (request.getLanguage() != null) {
            document.setLanguage(request.getLanguage());
        }
        if (request.getYear() != null) {
            document.setYear(request.getYear());
        }
        if (request.getVerified() != null) {
            document.setVerified(request.getVerified());
        }
        if (request.getProfessorId() != null) {
            Professor professor = professorRepository.findById(request.getProfessorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", request.getProfessorId()));
            document.setProfessor(professor);
        }
        if (request.getTags() != null) {
            document.getTags().clear();
            request.getTags().forEach(label -> {
                Tag tag = Tag.builder()
                        .document(document)
                        .label(sanitize(label.strip().toLowerCase()))
                        .build();
                document.getTags().add(tag);
            });
        }

        Document saved = documentRepository.save(document);
        meilisearchService.indexDocument(saved);
        return documentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void adminDelete(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        minioService.delete(document.getFileKey());
        meilisearchService.deleteDocument(document.getId());
        documentRepository.delete(document);
    }

    // --- Download with Redis buffer ---

    @Override
    public byte[] download(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        // Buffer in Redis — no DB write on each download
        redisTemplate.opsForValue().increment(DL_BUFFER_PREFIX + documentId);

        // Award 1 XP to author — skip if downloader is the author (anti-farming)
        if (document.getUser() != null && !document.getUser().getId().equals(userId)) {
            userService.addXp(document.getUser().getId(), 1);
        }

        return minioService.download(document.getFileKey());
    }

    @Override
    public PageResponse<DocumentResponse> getByUser(Long userId, Pageable pageable) {
        Page<Document> page = documentRepository.findByUserId(userId, pageable);
        List<DocumentResponse> content = page.getContent().stream()
                .filter(Document::isVerified)
                .map(documentMapper::toResponse)
                .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    /** Flush buffered download counts to DB every 5 minutes */
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void flushDownloadCounts() {
        var scanOptions = org.springframework.data.redis.core.ScanOptions.scanOptions()
                .match(DL_BUFFER_PREFIX + "*").count(100).build();

        try (var cursor = redisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String value = redisTemplate.opsForValue().getAndDelete(key);
                if (value == null) continue;

                long increment = Long.parseLong(value);
                Long docId = Long.parseLong(key.substring(DL_BUFFER_PREFIX.length()));
                documentRepository.incrementDownloadCount(docId, (int) increment);
            }
        }
    }
}

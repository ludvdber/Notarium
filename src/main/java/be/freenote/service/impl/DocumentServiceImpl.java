package be.freenote.service.impl;

import be.freenote.dto.request.CreateDocumentRequest;
import be.freenote.dto.request.UpdateDocumentRequest;
import be.freenote.dto.response.DocumentResponse;
import be.freenote.dto.response.PageResponse;
import be.freenote.entity.*;
import be.freenote.enums.Category;
import be.freenote.exception.ForbiddenException;
import be.freenote.mapper.DocumentMapper;
import be.freenote.repository.*;
import be.freenote.repository.Repositories;
import be.freenote.event.XpEvent;
import be.freenote.service.DocumentService;
import be.freenote.service.MeilisearchService;
import be.freenote.service.MinioService;
import be.freenote.service.PdfValidationService;
import be.freenote.service.StatsService;
import be.freenote.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
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
    private final PdfValidationService pdfValidationService;
    private final MeilisearchService meilisearchService;
    private final StatsService statsService;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public DocumentResponse create(CreateDocumentRequest request, MultipartFile file, Long userId) {
        byte[] pdfBytes = pdfValidationService.validate(file);

        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");
        Course course = Repositories.findByIdOrThrow(courseRepository, request.getCourseId(), "Course");

        Professor professor = null;
        if (request.getProfessorId() != null) {
            professor = Repositories.findByIdOrThrow(professorRepository, request.getProfessorId(), "Professor");
        }

        // Validate category against enum
        Category category;
        try {
            category = Category.valueOf(request.getCategory());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + request.getCategory());
        }

        String fileKey = UUID.randomUUID() + "/" + FileUtil.sanitizeFileName(file.getOriginalFilename());
        minioService.upload(fileKey, new ByteArrayInputStream(pdfBytes), pdfBytes.length, PDF_CONTENT_TYPE);

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
                .fileSize((long) pdfBytes.length)
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
        statsService.invalidateCache();
        // XP is awarded when admin verifies the document, not at upload — prevents spam farming

        log.info("Document uploaded: id={}, title='{}', user={}, size={}KB",
                saved.getId(), saved.getTitle(), userId, pdfBytes.length / 1024);

        return documentMapper.toResponse(saved);
    }

    @Override
    public DocumentResponse getById(Long id) {
        Document document = Repositories.findByIdOrThrow(documentRepository, id, "Document");
        return documentMapper.toResponse(document);
    }

    @Override
    public PageResponse<DocumentResponse> search(String query, Long sectionId, Long courseId, String category,
                                                   String sort, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            List<Long> ids = meilisearchService.search(query, courseId, category, sort, pageable);
            if (ids.isEmpty()) {
                return new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0);
            }
            // Preserve Meilisearch relevance/sort ordering — findAllById returns rows in DB order.
            // Section is post-filtered in Java because it's not a Meilisearch filterable attribute today.
            Map<Long, Document> byId = documentRepository.findAllById(ids).stream()
                    .filter(d -> sectionId == null
                            || (d.getCourse() != null
                                && d.getCourse().getSection() != null
                                && sectionId.equals(d.getCourse().getSection().getId())))
                    .collect(Collectors.toMap(Document::getId, d -> d));
            List<DocumentResponse> results = ids.stream()
                    .map(byId::get)
                    .filter(java.util.Objects::nonNull)
                    .map(documentMapper::toResponse)
                    .toList();
            return new PageResponse<>(results, pageable.getPageNumber(), pageable.getPageSize(),
                    results.size(), 1);
        }

        Category cat = category != null ? Category.valueOf(category) : null;
        Page<Document> page = documentRepository.findVerifiedFiltered(sectionId, courseId, cat, pageable);

        List<DocumentResponse> content = page.getContent().stream()
                .map(documentMapper::toResponse)
                .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional
    public void delete(Long documentId, Long userId) {
        Document document = Repositories.findByIdOrThrow(documentRepository, documentId, "Document");
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");

        boolean isAuthor = document.getUser() != null && document.getUser().getId().equals(userId);
        boolean isAdmin = "ADMIN".equals(user.getRole());
        if (!isAuthor && !isAdmin) {
            throw new ForbiddenException("You can only delete your own documents");
        }

        minioService.delete(document.getFileKey());
        meilisearchService.deleteDocument(document.getId());
        documentRepository.delete(document);
        statsService.invalidateCache();
        log.info("Document deleted: id={}, by user={}", documentId, userId);
    }

    @Override
    public List<DocumentResponse> getPopular() {
        return documentRepository.findTop10ByVerifiedTrueOrderByDownloadCountDesc().stream()
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
        Document document = Repositories.findByIdOrThrow(documentRepository, documentId, "Document");
        document.setVerified(true);
        Document saved = documentRepository.save(document);

        // Award XP to author on verification (not at upload) — prevents spam farming
        if (document.getUser() != null) {
            eventPublisher.publishEvent(new XpEvent.DocumentVerified(document.getUser().getId(), documentId));
        }
        statsService.invalidateCache();

        return documentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DocumentResponse adminUpdate(Long documentId, UpdateDocumentRequest request) {
        Document document = Repositories.findByIdOrThrow(documentRepository, documentId, "Document");

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            document.setTitle(sanitize(request.getTitle()));
        }
        if (request.getCourseId() != null) {
            Course course = Repositories.findByIdOrThrow(courseRepository, request.getCourseId(), "Course");
            document.setCourse(course);
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
            Professor professor = Repositories.findByIdOrThrow(professorRepository, request.getProfessorId(), "Professor");
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
        Document document = Repositories.findByIdOrThrow(documentRepository, documentId, "Document");
        minioService.delete(document.getFileKey());
        meilisearchService.deleteDocument(document.getId());
        documentRepository.delete(document);
        statsService.invalidateCache();
    }

    // --- Download with Redis buffer ---

    @Override
    public byte[] download(Long documentId, Long userId) {
        Document document = Repositories.findByIdOrThrow(documentRepository, documentId, "Document");

        // Buffer in Redis — no DB write on each download
        redisTemplate.opsForValue().increment(DL_BUFFER_PREFIX + documentId);

        // Award 1 XP to author — skip if downloader is the author (anti-farming)
        if (document.getUser() != null && !document.getUser().getId().equals(userId)) {
            eventPublisher.publishEvent(new XpEvent.DocumentDownloaded(document.getUser().getId(), documentId));
        }

        return minioService.download(document.getFileKey());
    }

    @Override
    public PageResponse<DocumentResponse> getByUser(Long userId, Pageable pageable) {
        Page<Document> page = documentRepository.findByUserIdAndVerifiedTrue(userId, pageable);
        List<DocumentResponse> content = page.getContent().stream()
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

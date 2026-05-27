package be.freenote.service.impl;

import be.freenote.dto.response.SectionResponse;
import be.freenote.entity.Course;
import be.freenote.entity.Document;
import be.freenote.entity.Section;
import be.freenote.exception.DuplicateResourceException;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.Repositories;
import be.freenote.repository.SectionRepository;
import be.freenote.service.MeilisearchService;
import be.freenote.service.MinioService;
import be.freenote.service.SectionService;
import be.freenote.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final DocumentRepository documentRepository;
    private final MinioService minioService;
    private final MeilisearchService meilisearchService;
    private final StatsService statsService;

    @Override
    public List<SectionResponse> getAll() {
        return sectionRepository.findAllApprovedWithDocCount().stream()
                .map(row -> new SectionResponse(
                        row.getId(),
                        row.getName(),
                        row.getIcon(),
                        row.getDocumentCount() != null ? row.getDocumentCount() : 0,
                        Boolean.TRUE.equals(row.getApproved())
                ))
                .toList();
    }

    @Override
    public List<SectionResponse> getAllForAdmin() {
        return sectionRepository.findAllWithDocCount().stream()
                .map(row -> new SectionResponse(
                        row.getId(),
                        row.getName(),
                        row.getIcon(),
                        row.getDocumentCount() != null ? row.getDocumentCount() : 0,
                        Boolean.TRUE.equals(row.getApproved())
                ))
                .toList();
    }

    @Override
    public SectionResponse getById(Long id) {
        Section section = Repositories.findByIdOrThrow(sectionRepository, id, "Section");
        long docCount = documentRepository.countBySectionId(id);
        return new SectionResponse(section.getId(), section.getName(), section.getIcon(), docCount, section.isApproved());
    }

    @Override
    @Transactional
    public SectionResponse create(String name, String icon) {
        String sanitized = requireName(name);
        if (sectionRepository.existsByNameIgnoreCase(sanitized)) {
            throw new DuplicateResourceException("A section with this name already exists");
        }
        Section section = Section.builder()
                .name(sanitized)
                .icon(sanitize(icon))
                .approved(true)
                .build();
        Section saved = sectionRepository.save(section);
        return new SectionResponse(saved.getId(), saved.getName(), saved.getIcon(), 0, saved.isApproved());
    }

    @Override
    @Transactional
    public SectionResponse approve(Long id) {
        Section section = Repositories.findByIdOrThrow(sectionRepository, id, "Section");
        section.setApproved(true);
        Section saved = sectionRepository.save(section);
        long docCount = documentRepository.countBySectionId(id);
        return new SectionResponse(saved.getId(), saved.getName(), saved.getIcon(), docCount, saved.isApproved());
    }

    @Override
    @Transactional
    public SectionResponse rename(Long id, String name, String icon) {
        Section section = Repositories.findByIdOrThrow(sectionRepository, id, "Section");
        String sanitized = requireName(name);
        if (!section.getName().equalsIgnoreCase(sanitized) && sectionRepository.existsByNameIgnoreCase(sanitized)) {
            throw new DuplicateResourceException("A section with this name already exists");
        }
        section.setName(sanitized);
        if (icon != null) {
            section.setIcon(sanitize(icon));
        }
        Section saved = sectionRepository.save(section);
        long docCount = documentRepository.countBySectionId(id);
        return new SectionResponse(saved.getId(), saved.getName(), saved.getIcon(), docCount, saved.isApproved());
    }

    @Override
    @Transactional
    public void adminDelete(Long id) {
        Section section = Repositories.findByIdOrThrow(sectionRepository, id, "Section");
        for (Course course : section.getCourses()) {
            for (Document doc : course.getDocuments()) {
                if (doc.getFileKey() != null) {
                    minioService.delete(doc.getFileKey());
                }
                meilisearchService.deleteDocument(doc.getId());
            }
        }
        sectionRepository.delete(section);
        statsService.invalidateCache();
        log.info("Section deleted by admin: id={}, name={}", id, section.getName());
    }

    private static String requireName(String name) {
        String sanitized = sanitize(name);
        if (sanitized == null || sanitized.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (sanitized.length() > 100) {
            throw new IllegalArgumentException("Name too long (max 100)");
        }
        return sanitized;
    }

    private static String sanitize(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}

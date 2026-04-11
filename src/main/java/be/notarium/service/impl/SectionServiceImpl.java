package be.notarium.service.impl;

import be.notarium.dto.response.SectionResponse;
import be.notarium.entity.Section;
import be.notarium.exception.ResourceNotFoundException;
import be.notarium.repository.DocumentRepository;
import be.notarium.repository.SectionRepository;
import be.notarium.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final DocumentRepository documentRepository;

    @Override
    public List<SectionResponse> getAll() {
        return sectionRepository.findAllApprovedWithDocCount().stream()
                .map(row -> new SectionResponse(
                        row.getId(),
                        row.getName(),
                        row.getIcon(),
                        row.getDocumentCount() != null ? row.getDocumentCount() : 0
                ))
                .toList();
    }

    @Override
    public SectionResponse getById(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
        long docCount = section.getCourses().stream()
                .mapToLong(c -> documentRepository.countByCourseId(c.getId()))
                .sum();
        return new SectionResponse(section.getId(), section.getName(), section.getIcon(), docCount);
    }

    @Override
    @Transactional
    public SectionResponse create(String name, String icon) {
        Section section = Section.builder()
                .name(name)
                .icon(icon)
                .build();
        Section saved = sectionRepository.save(section);
        return new SectionResponse(saved.getId(), saved.getName(), saved.getIcon(), 0);
    }

    @Override
    @Transactional
    public SectionResponse approve(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
        section.setApproved(true);
        Section saved = sectionRepository.save(section);
        long docCount = saved.getCourses().stream()
                .mapToLong(c -> documentRepository.countByCourseId(c.getId()))
                .sum();
        return new SectionResponse(saved.getId(), saved.getName(), saved.getIcon(), docCount);
    }
}

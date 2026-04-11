package be.notarium.service.impl;

import be.notarium.dto.request.ReportRequest;
import be.notarium.dto.response.PageResponse;
import be.notarium.dto.response.ReportResponse;
import be.notarium.entity.Document;
import be.notarium.entity.Report;
import be.notarium.entity.User;
import be.notarium.enums.ReportStatus;
import be.notarium.exception.ResourceNotFoundException;
import be.notarium.mapper.ReportMapper;
import be.notarium.repository.DocumentRepository;
import be.notarium.repository.ReportRepository;
import be.notarium.repository.UserRepository;
import be.notarium.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final ReportMapper reportMapper;

    @Override
    @Transactional
    public void create(Long userId, Long documentId, ReportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        Report report = Report.builder()
                .document(document)
                .user(user)
                .reason(request.getReason())
                .build();

        reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> listPending(Pageable pageable) {
        Page<Report> page = reportRepository.findByStatus(ReportStatus.PENDING, pageable);
        List<ReportResponse> content = page.getContent().stream()
                .map(reportMapper::toResponse)
                .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional
    public void resolve(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));
        report.setStatus(ReportStatus.RESOLVED);
        reportRepository.save(report);
    }

    @Override
    @Transactional
    public void dismiss(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));
        report.setStatus(ReportStatus.DISMISSED);
        reportRepository.save(report);
    }
}

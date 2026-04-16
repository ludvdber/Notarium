package be.freenote.service.impl;

import be.freenote.dto.request.ReportRequest;
import be.freenote.dto.response.PageResponse;
import be.freenote.dto.response.ReportResponse;
import be.freenote.entity.Document;
import be.freenote.entity.Report;
import be.freenote.entity.User;
import be.freenote.enums.ReportStatus;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.mapper.ReportMapper;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.ReportRepository;
import be.freenote.repository.UserRepository;
import be.freenote.service.ReportService;
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

package be.freenote.service.impl;

import be.freenote.dto.request.ReportRequest;
import be.freenote.dto.response.PageResponse;
import be.freenote.dto.response.ReportResponse;
import be.freenote.entity.Document;
import be.freenote.entity.Report;
import be.freenote.entity.User;
import be.freenote.enums.ReportStatus;
import be.freenote.mapper.ReportMapper;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.Repositories;
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
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");
        Document document = Repositories.findByIdOrThrow(documentRepository, documentId, "Document");

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
        return PageResponse.from(page, content);
    }

    @Override
    @Transactional
    public void resolve(Long reportId) {
        Report report = Repositories.findByIdOrThrow(reportRepository, reportId, "Report");
        report.setStatus(ReportStatus.RESOLVED);
        reportRepository.save(report);
    }

    @Override
    @Transactional
    public void dismiss(Long reportId) {
        Report report = Repositories.findByIdOrThrow(reportRepository, reportId, "Report");
        report.setStatus(ReportStatus.DISMISSED);
        reportRepository.save(report);
    }
}

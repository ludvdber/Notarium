package be.notarium.service;

import be.notarium.dto.request.ReportRequest;
import be.notarium.dto.response.PageResponse;
import be.notarium.dto.response.ReportResponse;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    void create(Long userId, Long documentId, ReportRequest request);
    PageResponse<ReportResponse> listPending(Pageable pageable);
    void resolve(Long reportId);
    void dismiss(Long reportId);
}

package be.freenote.service;

import be.freenote.dto.request.ReportRequest;
import be.freenote.dto.response.PageResponse;
import be.freenote.dto.response.ReportResponse;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    void create(Long userId, Long documentId, ReportRequest request);
    PageResponse<ReportResponse> listPending(Pageable pageable);
    void resolve(Long reportId);
    void dismiss(Long reportId);
}

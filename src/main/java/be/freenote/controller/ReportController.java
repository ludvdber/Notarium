package be.freenote.controller;

import be.freenote.security.SecurityUtils;
import be.freenote.dto.request.ReportRequest;
import be.freenote.security.ratelimit.RateLimit;
import be.freenote.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents/{documentId}/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @RateLimit(max = 3, window = 3600)
    public ResponseEntity<Void> create(Authentication authentication,
                                        @PathVariable Long documentId,
                                        @Valid @RequestBody ReportRequest request) {
        Long userId = SecurityUtils.currentUserId(authentication);
        reportService.create(userId, documentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

package be.notarium.controller;

import be.notarium.dto.request.ReportRequest;
import be.notarium.security.ratelimit.RateLimit;
import be.notarium.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents/{documentId}/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Document report operations")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @RateLimit(max = 3, window = 3600)
    @Operation(summary = "Report a document",
               description = "Creates a report for a document with a reason. Requires verified role.")
    public ResponseEntity<Void> create(Authentication authentication,
                                        @PathVariable Long documentId,
                                        @Valid @RequestBody ReportRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        reportService.create(userId, documentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

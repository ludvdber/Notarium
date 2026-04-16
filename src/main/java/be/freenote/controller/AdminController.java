package be.freenote.controller;

import be.freenote.dto.request.UpdateDocumentRequest;
import be.freenote.dto.response.CourseResponse;
import be.freenote.dto.response.DocumentResponse;
import be.freenote.dto.response.PageResponse;
import be.freenote.dto.response.ProfessorResponse;
import be.freenote.dto.response.ReportResponse;
import be.freenote.dto.response.SectionResponse;
import be.freenote.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administration endpoints (ADMIN role required)")
public class AdminController {

    private final DocumentService documentService;
    private final CourseService courseService;
    private final ProfessorService professorService;
    private final ReportService reportService;
    private final SectionService sectionService;

    // --- Documents ---

    @GetMapping("/documents/pending")
    @Operation(summary = "Get unverified documents",
               description = "Returns all documents that have not been verified yet.")
    public ResponseEntity<List<DocumentResponse>> getPendingDocuments() {
        return ResponseEntity.ok(documentService.getUnverified());
    }

    @PutMapping("/documents/{id}/verify")
    @Operation(summary = "Verify a document",
               description = "Marks a document as verified by an admin.")
    public ResponseEntity<DocumentResponse> verifyDocument(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.verify(id));
    }

    @PutMapping("/documents/{id}")
    @Operation(summary = "Update a document (admin)",
               description = "Updates document metadata: title, tags, category, language, year, professor, verified status.")
    public ResponseEntity<DocumentResponse> updateDocument(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateDocumentRequest request) {
        return ResponseEntity.ok(documentService.adminUpdate(id, request));
    }

    @DeleteMapping("/documents/{id}")
    @Operation(summary = "Delete a document (admin)",
               description = "Permanently deletes a document, its file from storage, and its search index entry.")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Courses ---

    @GetMapping("/courses/pending")
    @Operation(summary = "Get pending courses",
               description = "Returns all courses awaiting admin approval.")
    public ResponseEntity<List<CourseResponse>> getPendingCourses() {
        return ResponseEntity.ok(courseService.getPending());
    }

    @PutMapping("/courses/{id}/approve")
    @Operation(summary = "Approve a course",
               description = "Approves a course so it becomes visible to users.")
    public ResponseEntity<CourseResponse> approveCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.approve(id));
    }

    // --- Professors ---

    @GetMapping("/professors/pending")
    @Operation(summary = "Get pending professors",
               description = "Returns all professors awaiting admin approval.")
    public ResponseEntity<List<ProfessorResponse>> getPendingProfessors() {
        return ResponseEntity.ok(professorService.getPending());
    }

    @PutMapping("/professors/{id}/approve")
    @Operation(summary = "Approve a professor",
               description = "Approves a professor so it becomes selectable by users.")
    public ResponseEntity<ProfessorResponse> approveProfessor(@PathVariable Long id) {
        return ResponseEntity.ok(professorService.approve(id));
    }

    // --- Sections ---

    @PostMapping("/sections")
    @Operation(summary = "Create a section",
               description = "Creates a new section (unapproved by default).")
    public ResponseEntity<SectionResponse> createSection(@RequestParam String name,
                                                          @RequestParam(required = false) String icon) {
        return ResponseEntity.status(201).body(sectionService.create(name, icon));
    }

    @PutMapping("/sections/{id}/approve")
    @Operation(summary = "Approve a section",
               description = "Approves a section so it becomes visible to users.")
    public ResponseEntity<SectionResponse> approveSection(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.approve(id));
    }

    // --- Reports ---

    @GetMapping("/reports/pending")
    @Operation(summary = "Get pending reports",
               description = "Returns all reports with PENDING status, paginated.")
    public ResponseEntity<PageResponse<ReportResponse>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reportService.listPending(PageRequest.of(page, size)));
    }

    @PutMapping("/reports/{id}/resolve")
    @Operation(summary = "Resolve a report",
               description = "Marks a report as resolved.")
    public ResponseEntity<Void> resolveReport(@PathVariable Long id) {
        reportService.resolve(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reports/{id}/dismiss")
    @Operation(summary = "Dismiss a report",
               description = "Dismisses a report as not actionable.")
    public ResponseEntity<Void> dismissReport(@PathVariable Long id) {
        reportService.dismiss(id);
        return ResponseEntity.ok().build();
    }
}

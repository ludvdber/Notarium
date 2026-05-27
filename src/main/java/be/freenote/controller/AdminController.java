package be.freenote.controller;

import be.freenote.security.SecurityUtils;
import be.freenote.dto.request.CreateCourseRequest;
import be.freenote.dto.request.UpdateDocumentRequest;
import be.freenote.dto.response.CourseResponse;
import be.freenote.dto.response.DocumentResponse;
import be.freenote.dto.response.DonationResponse;
import be.freenote.dto.response.PageResponse;
import be.freenote.dto.response.ProfessorResponse;
import be.freenote.dto.response.ReportResponse;
import be.freenote.dto.response.SectionResponse;
import be.freenote.dto.response.UserResponse;
import be.freenote.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final UserService userService;
    private final DonationService donationService;

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

    @GetMapping("/courses")
    @Operation(summary = "List all courses (admin)",
               description = "Returns every course, approved or not, grouped by section.")
    public ResponseEntity<List<CourseResponse>> listCourses() {
        return ResponseEntity.ok(courseService.getAllForAdmin());
    }

    @GetMapping("/courses/pending")
    @Operation(summary = "Get pending courses",
               description = "Returns all courses awaiting admin approval.")
    public ResponseEntity<List<CourseResponse>> getPendingCourses() {
        return ResponseEntity.ok(courseService.getPending());
    }

    @PostMapping("/courses")
    @Operation(summary = "Create a course (admin)",
               description = "Creates a course approved immediately, bypassing the pending queue.")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(201).body(courseService.adminCreate(request));
    }

    @PutMapping("/courses/{id}/approve")
    @Operation(summary = "Approve a course",
               description = "Approves a course so it becomes visible to users.")
    public ResponseEntity<CourseResponse> approveCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.approve(id));
    }

    @PatchMapping("/courses/{id}")
    @Operation(summary = "Rename a course",
               description = "Updates the name of an existing course.")
    public ResponseEntity<CourseResponse> renameCourse(@PathVariable Long id,
                                                       @RequestParam String name) {
        return ResponseEntity.ok(courseService.rename(id, name));
    }

    @DeleteMapping("/courses/{id}")
    @Operation(summary = "Delete a course",
               description = "Deletes a course and all its documents (MinIO files and Meilisearch entries included).")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.adminDelete(id);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/sections")
    @Operation(summary = "List all sections (admin)",
               description = "Returns every section, approved or not, with document counts.")
    public ResponseEntity<List<SectionResponse>> listSections() {
        return ResponseEntity.ok(sectionService.getAllForAdmin());
    }

    @PostMapping("/sections")
    @Operation(summary = "Create a section",
               description = "Creates a new section (approved immediately when created by an admin).")
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

    @PatchMapping("/sections/{id}")
    @Operation(summary = "Rename a section",
               description = "Updates the name (and optionally the icon) of an existing section.")
    public ResponseEntity<SectionResponse> renameSection(@PathVariable Long id,
                                                          @RequestParam String name,
                                                          @RequestParam(required = false) String icon) {
        return ResponseEntity.ok(sectionService.rename(id, name, icon));
    }

    @DeleteMapping("/sections/{id}")
    @Operation(summary = "Delete a section",
               description = "Deletes a section, all its courses, and all their documents (MinIO files and Meilisearch entries included).")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionService.adminDelete(id);
        return ResponseEntity.noContent().build();
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

    // --- Users ---

    @GetMapping("/users")
    @Operation(summary = "Search users (admin)",
               description = "Returns users whose username matches the query. Empty query returns the first `limit` users alphabetically.")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(userService.adminSearchUsers(q, limit));
    }

    @PutMapping("/users/{id}/verify")
    @Operation(summary = "Manually verify a user",
               description = "Marks a user as verified without the @isfce.be email flow. Promotes USER role to VERIFIED.")
    public ResponseEntity<UserResponse> verifyUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.adminVerifyUser(id));
    }

    @PutMapping("/users/{id}/unverify")
    @Operation(summary = "Revoke a user's verification",
               description = "Removes the verified flag. VERIFIED role is demoted to USER (ADMIN is preserved).")
    public ResponseEntity<UserResponse> unverifyUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.adminUnverifyUser(id));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update a user's role",
               description = "Sets the role to USER, VERIFIED or ADMIN. VERIFIED and ADMIN roles imply verified=true.")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        return ResponseEntity.ok(userService.adminUpdateRole(id, role));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user (admin)",
               description = "Anonymizes the user's documents (they stay accessible as 'Anonyme'), detaches reports, removes the account.")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.adminDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // --- Donations ---

    @GetMapping("/donations")
    @Operation(summary = "List donations (admin)",
               description = "Returns all donations (Ko-fi + manual admin grants), most recent first.")
    public ResponseEntity<PageResponse<DonationResponse>> listDonations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(donationService.listAll(PageRequest.of(page, size)));
    }

    @PostMapping("/users/{id}/grant-ad-free")
    @Operation(summary = "Manually grant ad-free days",
               description = "Compensates a missed Ko-fi reward or gifts ad-free time. Extends any existing ad-free period instead of replacing it.")
    public ResponseEntity<DonationResponse> grantAdFree(@PathVariable Long id,
                                                         @RequestParam int days,
                                                         Authentication authentication) {
        Long adminId = SecurityUtils.currentUserId(authentication);
        return ResponseEntity.ok(donationService.grantAdFree(id, days, adminId));
    }
}

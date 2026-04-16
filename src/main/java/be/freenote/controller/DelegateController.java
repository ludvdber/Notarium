package be.freenote.controller;

import be.freenote.dto.request.AssignDelegateRequest;
import be.freenote.dto.request.EndDelegateRequest;
import be.freenote.dto.response.DelegateHistoryResponse;
import be.freenote.dto.response.DelegateMember;
import be.freenote.dto.response.DelegateResponse;
import be.freenote.service.DelegateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Delegates", description = "Section delegates")
public class DelegateController {

    private final DelegateService delegateService;

    // ── Public ──────────────────────────────────────────────────────────

    @GetMapping("/api/delegates")
    @Operation(summary = "Get active delegates",
               description = "Returns the list of active delegates grouped by section. Public endpoint.")
    public ResponseEntity<List<DelegateResponse>> getActiveDelegates() {
        return ResponseEntity.ok(delegateService.getActiveDelegates());
    }

    @GetMapping("/api/delegates/user/{userId}")
    @Operation(summary = "Get delegate history for a user",
               description = "Returns the full delegate history (active + past) for a given user. Public endpoint.")
    public ResponseEntity<List<DelegateHistoryResponse>> getUserHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(delegateService.getHistory(userId));
    }

    // ── Admin ───────────────────────────────────────────────────────────

    @GetMapping("/api/admin/delegates")
    @Operation(summary = "Get all mandates",
               description = "Returns every delegate mandate (active and past). Admin only.")
    public ResponseEntity<List<DelegateMember>> getAllMandates() {
        return ResponseEntity.ok(delegateService.getAllMandates());
    }

    @PostMapping("/api/admin/delegates")
    @Operation(summary = "Assign a delegate",
               description = "Creates a new delegate mandate. Fails with 409 if the user already has an active mandate in another section.")
    public ResponseEntity<DelegateMember> assignDelegate(@Valid @RequestBody AssignDelegateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(delegateService.assignDelegate(request));
    }

    @PatchMapping("/api/admin/delegates/{id}")
    @Operation(summary = "End a delegate mandate",
               description = "Sets the end date on an active mandate. Fails if already ended or if end date is before start date.")
    public ResponseEntity<DelegateMember> endDelegate(
            @PathVariable Long id,
            @Valid @RequestBody EndDelegateRequest request) {
        return ResponseEntity.ok(delegateService.endDelegate(id, request));
    }

    @DeleteMapping("/api/admin/delegates/{id}")
    @Operation(summary = "Delete a delegate mandate",
               description = "Permanently removes a mandate entry (use sparingly — prefer ending instead). Admin only.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMandate(@PathVariable Long id) {
        delegateService.deleteMandate(id);
    }
}

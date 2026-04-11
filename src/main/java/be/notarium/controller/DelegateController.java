package be.notarium.controller;

import be.notarium.dto.response.DelegateResponse;
import be.notarium.service.DelegateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delegates")
@RequiredArgsConstructor
@Tag(name = "Delegates", description = "Section delegates")
public class DelegateController {

    private final DelegateService delegateService;

    @GetMapping
    @Operation(summary = "Get active delegates",
               description = "Returns the list of active delegates grouped by section. Public endpoint.")
    public ResponseEntity<List<DelegateResponse>> getActiveDelegates() {
        return ResponseEntity.ok(delegateService.getActiveDelegates());
    }
}

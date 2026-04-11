package be.notarium.controller;

import be.notarium.dto.response.SectionResponse;
import be.notarium.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@Tag(name = "Sections", description = "Academic sections")
public class SectionController {

    private final SectionService sectionService;

    @GetMapping
    @Operation(summary = "Get all approved sections",
               description = "Returns all approved sections with their document count. Public endpoint.")
    public ResponseEntity<List<SectionResponse>> getAll() {
        return ResponseEntity.ok(sectionService.getAll());
    }
}

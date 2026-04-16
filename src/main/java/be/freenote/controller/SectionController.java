package be.freenote.controller;

import be.freenote.dto.response.SectionResponse;
import be.freenote.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
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
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(2)).cachePublic())
                .body(sectionService.getAll());
    }
}

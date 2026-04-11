package be.notarium.controller;

import be.notarium.dto.response.StatsResponse;
import be.notarium.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Platform statistics")
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    @Operation(summary = "Get platform statistics",
               description = "Returns aggregated stats (total docs, downloads, contributors, courses, weekly uploads). Cached in Redis for 5 minutes. Public endpoint.")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }
}

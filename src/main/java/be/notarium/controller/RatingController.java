package be.notarium.controller;

import be.notarium.dto.request.RateRequest;
import be.notarium.security.ratelimit.RateLimit;
import be.notarium.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents/{docId}/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Document rating operations")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @RateLimit(max = 10, window = 3600)
    @Operation(summary = "Rate a document", description = "Upserts a rating (1-5) for the document. Awards 2 XP per star to the document author on first rating.")
    public ResponseEntity<Void> rate(Authentication authentication,
                                      @PathVariable Long docId,
                                      @Valid @RequestBody RateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        ratingService.rate(userId, docId, request.getScore());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/average")
    @Operation(summary = "Get average rating", description = "Returns the average rating score for the document.")
    public ResponseEntity<Double> getAverage(@PathVariable Long docId) {
        Double avg = ratingService.getAverageRating(docId);
        return ResponseEntity.ok(avg != null ? avg : 0.0);
    }
}

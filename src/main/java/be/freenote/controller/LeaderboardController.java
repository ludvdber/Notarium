package be.freenote.controller;

import be.freenote.dto.response.LeaderboardEntry;
import be.freenote.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "XP leaderboard")
public class LeaderboardController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get leaderboard",
               description = "Returns the top users ranked by XP with their badges and document count. Public endpoint.")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(
            @RequestParam(defaultValue = "10") int size) {
        int clamped = Math.max(1, Math.min(size, 100));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(2)).cachePublic())
                .body(userService.getLeaderboard(clamped));
    }
}

package be.notarium.controller;

import be.notarium.dto.response.LeaderboardEntry;
import be.notarium.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "XP leaderboard")
public class LeaderboardController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get leaderboard",
               description = "Returns the top 10 users ranked by XP with their badges and document count. Public endpoint.")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard());
    }
}

package be.freenote.controller;

import be.freenote.dto.response.DocumentResponse;
import be.freenote.dto.response.PageResponse;
import be.freenote.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "User favorite documents")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // POST toggle plutôt que PUT+DELETE séparés : le frontend n'a qu'un seul bouton
    // favori, et le toggle en un seul appel simplifie la gestion d'état côté client.
    @PostMapping("/{docId}")
    @Operation(summary = "Toggle favorite", description = "Adds or removes a document from the user's favorites. Returns whether the document is now favorited.")
    public ResponseEntity<Map<String, Boolean>> toggle(Authentication authentication,
                                                        @PathVariable Long docId) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isFavorite = favoriteService.toggle(userId, docId);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }

    @GetMapping
    @Operation(summary = "Get favorites", description = "Returns the current user's favorite documents, paginated.")
    public ResponseEntity<PageResponse<DocumentResponse>> getFavorites(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(favoriteService.getFavorites(userId, PageRequest.of(page, size)));
    }
}

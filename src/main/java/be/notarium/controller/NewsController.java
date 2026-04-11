package be.notarium.controller;

import be.notarium.dto.response.NewsItem;
import be.notarium.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "News", description = "ISFCE news feed")
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    @Operation(summary = "Get ISFCE news",
               description = "Fetches and returns the latest news from the ISFCE blog RSS feed. Cached in Redis for 30 minutes. Public endpoint.")
    public ResponseEntity<List<NewsItem>> getNews() {
        return ResponseEntity.ok(newsService.getNews());
    }
}

package be.freenote.controller;

import be.freenote.dto.response.NewsItem;
import be.freenote.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
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
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(2)).cachePublic())
                .body(newsService.getNews());
    }
}

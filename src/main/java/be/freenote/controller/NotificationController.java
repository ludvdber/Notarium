package be.freenote.controller;

import be.freenote.dto.response.NotificationResponse;
import be.freenote.dto.response.PageResponse;
import be.freenote.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Per-user notification queue + live SSE stream")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List my notifications (paginated, newest first)")
    public ResponseEntity<PageResponse<NotificationResponse>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.list(userId, PageRequest.of(page, size)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Unread notification count")
    public ResponseEntity<Long> unreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.unreadCount(userId));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all my notifications as read")
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }

    /** Server-Sent Events stream. The browser reconnects automatically when the 30-min
     *  timeout fires, so the client doesn't need any extra wiring. */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Live notification stream (SSE)")
    public SseEmitter stream(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return notificationService.subscribe(userId);
    }
}

package be.freenote.service;

import be.freenote.dto.response.NotificationResponse;
import be.freenote.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface NotificationService {

    /** Persists + pushes (via SSE) a notification for one user. */
    void push(Long userId, String type, Map<String, Object> payload);

    PageResponse<NotificationResponse> list(Long userId, Pageable pageable);

    long unreadCount(Long userId);

    void markAllRead(Long userId);

    /** Opens an SSE stream for the given user. The emitter times out after 30 min and the
     *  client auto-reconnects with EventSource. */
    SseEmitter subscribe(Long userId);
}

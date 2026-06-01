package be.freenote.service.impl;

import be.freenote.dto.response.DocumentResponse;
import be.freenote.entity.Document;
import be.freenote.mapper.DocumentMapper;
import be.freenote.repository.DocumentRepository;
import be.freenote.service.RecentDocsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-user "recently opened documents" trail stored in Redis (list, newest first).
 * Duplicates are stripped on write so reopening a doc bumps it to the top without growing the list.
 * TTL of 90 days keeps the cache bounded — if a user disappears, their trail eventually ages out.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecentDocsServiceImpl implements RecentDocsService {

    private static final int MAX_STORED = 20;
    private static final Duration TTL = Duration.ofDays(90);

    private final RedisTemplate<String, Object> redisTemplate;
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    @Override
    public void recordVisit(Long userId, Long docId) {
        if (userId == null || docId == null) return;
        String key = key(userId);
        Object id = docId.toString();
        // Remove any existing occurrence, then push to the front, then trim + refresh TTL.
        redisTemplate.opsForList().remove(key, 0, id);
        redisTemplate.opsForList().leftPush(key, id);
        redisTemplate.opsForList().trim(key, 0, MAX_STORED - 1);
        redisTemplate.expire(key, TTL);
    }

    @Override
    public List<DocumentResponse> getRecent(Long userId, int max) {
        if (userId == null) return List.of();
        int clamped = Math.min(Math.max(max, 1), MAX_STORED);
        List<Object> raw = redisTemplate.opsForList().range(key(userId), 0, clamped - 1);
        if (raw == null || raw.isEmpty()) return List.of();

        List<Long> ids = raw.stream()
                .map(Object::toString)
                .map(s -> {
                    try { return Long.parseLong(s); }
                    catch (NumberFormatException e) { return null; }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        if (ids.isEmpty()) return List.of();

        // Both verified and unverified docs surface here (verification is a visual aid only);
        // recency order from Redis is preserved.
        Map<Long, Document> byId = new HashMap<>();
        for (Document d : documentRepository.findAllById(ids)) {
            byId.put(d.getId(), d);
        }
        return ids.stream()
                .map(byId::get)
                .filter(java.util.Objects::nonNull)
                .map(documentMapper::toResponse)
                .toList();
    }

    private static String key(Long userId) {
        return "user:" + userId + ":recent-docs";
    }
}

package be.freenote.service.impl;

import be.freenote.dto.response.StatsResponse;
import be.freenote.repository.CourseRepository;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.UserRepository;
import be.freenote.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final String CACHE_KEY = "stats:global";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public StatsResponse getStats() {
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached instanceof StatsResponse stats) {
            return stats;
        }

        long totalDocs = documentRepository.count();
        long totalDownloads = documentRepository.sumDownloadCount();
        long totalContributors = userRepository.count();
        long totalCourses = courseRepository.count();
        long weekUploads = documentRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(7));

        StatsResponse stats = new StatsResponse(totalDocs, totalDownloads, totalContributors,
                totalCourses, weekUploads);

        redisTemplate.opsForValue().set(CACHE_KEY, stats, CACHE_TTL);
        return stats;
    }
}

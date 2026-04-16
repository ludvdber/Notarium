package be.freenote.service;

import be.freenote.service.impl.RateLimitServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceImplTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    @InjectMocks private RateLimitServiceImpl rateLimitService;

    @Test
    void shouldAllowWhenUnderLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate:test-key")).thenReturn(1L);

        boolean result = rateLimitService.isAllowed("test-key", 5, 60);

        assertThat(result).isTrue();
        verify(redisTemplate).expire("rate:test-key", Duration.ofSeconds(60));
    }

    @Test
    void shouldAllowWhenAtLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate:test-key")).thenReturn(5L);

        boolean result = rateLimitService.isAllowed("test-key", 5, 60);

        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenOverLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate:test-key")).thenReturn(6L);

        boolean result = rateLimitService.isAllowed("test-key", 5, 60);

        assertThat(result).isFalse();
    }

    @Test
    void shouldSetExpireOnlyOnFirstIncrement() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate:test-key")).thenReturn(3L);

        rateLimitService.isAllowed("test-key", 5, 60);

        // expire should NOT be called when count > 1
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }
}

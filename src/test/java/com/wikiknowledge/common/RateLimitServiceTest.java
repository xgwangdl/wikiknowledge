package com.wikiknowledge.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rateLimitService = new RateLimitService(redisTemplate, 2, 100);
    }

    @Test
    void allowsRequestsWithinLimit() {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        rateLimitService.check("alice", "127.0.0.1");
    }

    @Test
    void rejectsWhenMinuteLimitExceeded() {
        when(valueOperations.increment(anyString())).thenReturn(3L);
        assertThatThrownBy(() -> rateLimitService.check("alice", "127.0.0.1"))
                .isInstanceOf(RateLimitException.class);
    }
}

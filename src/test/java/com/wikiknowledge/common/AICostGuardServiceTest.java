package com.wikiknowledge.common;

import com.wikiknowledge.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AICostGuardServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private AICostGuardService aiCostGuardService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        aiCostGuardService = new AICostGuardService(redisTemplate, 2, 2000);
    }

    @Test
    void allowsCallsWithinDailyLimit() {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        assertThatCode(() -> aiCostGuardService.check("alice", "问题"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsWhenDailyLimitExceeded() {
        when(valueOperations.increment(anyString())).thenReturn(3L);
        assertThatThrownBy(() -> aiCostGuardService.check("alice", "问题"))
                .isInstanceOf(RateLimitException.class);
    }

    @Test
    void rejectsTooLongQuestion() {
        assertThatThrownBy(() -> aiCostGuardService.check("alice", "a".repeat(2001)))
                .isInstanceOf(BusinessException.class);
    }
}

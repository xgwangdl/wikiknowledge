package com.wikiknowledge.common;

import com.wikiknowledge.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

/** AI 成本控制服务：限制每日调用次数与单次问题长度。 */
@Service
public class AICostGuardService {

    private static final String DAILY_KEY_PREFIX = "ai:cost:daily:";

    private final StringRedisTemplate redisTemplate;
    private final int maxDailyCalls;
    private final int maxQuestionLength;

    public AICostGuardService(StringRedisTemplate redisTemplate,
                              @Value("${app.ai-cost.max-daily-calls:100}") int maxDailyCalls,
                              @Value("${app.ai-cost.max-question-length:2000}") int maxQuestionLength) {
        this.redisTemplate = redisTemplate;
        this.maxDailyCalls = maxDailyCalls;
        this.maxQuestionLength = maxQuestionLength;
    }

    /**
     * 校验 AI 调用配额与问题长度。
     *
     * @param username 当前用户
     * @param question 用户问题
     */
    public void check(String username, String question) {
        if (question != null && question.length() > maxQuestionLength) {
            throw new BusinessException("QUESTION_TOO_LONG", "问题过长");
        }
        String key = DAILY_KEY_PREFIX + username + ":" + LocalDate.now();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofDays(1));
        }
        if (count != null && count > maxDailyCalls) {
            throw new RateLimitException("今日 AI 调用次数已用完");
        }
    }
}

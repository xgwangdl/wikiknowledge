package com.wikiknowledge.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Service
public class RateLimitService {

    private static final String MINUTE_PREFIX = "ratelimit:minute:";
    private static final String DAY_PREFIX = "ratelimit:day:";

    private final StringRedisTemplate redisTemplate;
    private final int perMinute;
    private final int perDay;

    public RateLimitService(StringRedisTemplate redisTemplate,
                            @Value("${app.rate-limit.per-minute:20}") int perMinute,
                            @Value("${app.rate-limit.per-day:500}") int perDay) {
        this.redisTemplate = redisTemplate;
        this.perMinute = perMinute;
        this.perDay = perDay;
    }

    public void check(String username, String ip) {
        String minuteKey = MINUTE_PREFIX + username + ":" + ip + ":" + minuteBucket();
        Long minuteCount = redisTemplate.opsForValue().increment(minuteKey);
        if (minuteCount != null && minuteCount == 1L) {
            redisTemplate.expire(minuteKey, Duration.ofMinutes(1));
        }
        if (minuteCount != null && minuteCount > perMinute) {
            throw new RateLimitException("请求过于频繁，请稍后再试");
        }

        String dayKey = DAY_PREFIX + username + ":" + ip + ":" + LocalDate.now();
        Long dayCount = redisTemplate.opsForValue().increment(dayKey);
        if (dayCount != null && dayCount == 1L) {
            redisTemplate.expire(dayKey, Duration.ofDays(1));
        }
        if (dayCount != null && dayCount > perDay) {
            throw new RateLimitException("今日请求次数已用完");
        }
    }

    private long minuteBucket() {
        return System.currentTimeMillis() / 60_000;
    }
}

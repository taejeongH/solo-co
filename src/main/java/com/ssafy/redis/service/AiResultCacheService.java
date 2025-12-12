package com.ssafy.redis.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiResultCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration TTL = Duration.ofMinutes(30);

    public void save(String aiResultId, Object value) {
        redisTemplate.opsForValue()
                .set(key(aiResultId), value, TTL);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String aiResultId, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key(aiResultId));
        if (value == null) {
            throw new RuntimeException("AI 추천 결과가 만료되었습니다.");
        }
        return (T) value;
    }

    private String key(String aiResultId) {
        return "ai:result:" + aiResultId;
    }
}

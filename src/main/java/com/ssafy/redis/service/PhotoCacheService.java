package com.ssafy.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PhotoCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PHOTO_CACHE_PREFIX = "photo_s3_key::";
    private static final long CACHE_DURATION_DAYS = 4;

    public String getS3Key(String googlePlaceId, String photoReference) {
        String key = buildKey(googlePlaceId, photoReference);
        return redisTemplate.opsForValue().get(key);
    }

    public void cacheS3Key(String googlePlaceId, String photoReference, String s3Key) {
        String key = buildKey(googlePlaceId, photoReference);
        redisTemplate.opsForValue().set(key, s3Key, CACHE_DURATION_DAYS, TimeUnit.DAYS);
    }

    private String buildKey(String googlePlaceId, String photoReference) {
        return PHOTO_CACHE_PREFIX + googlePlaceId + "::" + photoReference;
    }
}

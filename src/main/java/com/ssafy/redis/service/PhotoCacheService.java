package com.ssafy.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PhotoCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PHOTO_CACHE_PREFIX = "photo_s3_url::";
    private static final long CACHE_DURATION_DAYS = 4;

    public String getS3Url(String googlePlaceId, String photoReference) {
        String key = buildKey(googlePlaceId, photoReference);
        return redisTemplate.opsForValue().get(key);
    }

    public void cacheS3Url(String googlePlaceId, String photoReference, String s3Url) {
        String key = buildKey(googlePlaceId, photoReference);
        redisTemplate.opsForValue().set(key, s3Url, CACHE_DURATION_DAYS, TimeUnit.DAYS);
    }

    private String buildKey(String googlePlaceId, String photoReference) {
        return PHOTO_CACHE_PREFIX + googlePlaceId + "::" + photoReference;
    }
}

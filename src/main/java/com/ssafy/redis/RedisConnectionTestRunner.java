package com.ssafy.redis;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisConnectionTestRunner implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(String... args) {
        redisTemplate.opsForValue().set("redis-test", "ok");
        String value = redisTemplate.opsForValue().get("redis-test");
        System.out.println("🔥 Redis test value = " + value);
    }
}
package com.ledger.engine.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/redis")
public class RedisController {

    private final StringRedisTemplate redisTemplate;

    public RedisController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<String> checkRedis() {
        String response = redisTemplate.getConnectionFactory()
                .getConnection()
                .ping();

        return ResponseEntity.ok("Redis status: " + response);
    }
}
package io.orchestra.cloud.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.orchestra.core.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-based implementation of IdempotencyService.
 * Provides distributed caching capabilities suitable for multi-instance production environments.
 * 
 * This implementation is automatically selected when Redis is available on the classpath,
 * replacing the in-memory implementation from orchestra-core.
 */
@Service
@Primary
@ConditionalOnClass(StringRedisTemplate.class)
@RequiredArgsConstructor
@Slf4j
public class RedisIdempotencyService implements IdempotencyService {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public <T> Optional<T> getCachedResponse(String idempotencyKey, Class<T> responseType) {
        String cachedValue = redisTemplate.opsForValue().get(idempotencyKey);
        
        if (cachedValue == null) {
            return Optional.empty();
        }
        
        // Skip "LOCKED" sentinel value
        if ("LOCKED".equals(cachedValue)) {
            return Optional.empty();
        }
        
        try {
            T response = objectMapper.readValue(cachedValue, responseType);
            log.info("Redis cache hit for idempotency key: {}", idempotencyKey);
            return Optional.of(response);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing cached response from Redis for key {}: {}", idempotencyKey, e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public <T> void cacheResponse(String idempotencyKey, T response, Duration ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(idempotencyKey, jsonValue, ttl);
            log.debug("Cached response in Redis for idempotency key: {} with TTL: {}", idempotencyKey, ttl);
        } catch (JsonProcessingException e) {
            log.error("Error serializing response for Redis caching: {}", e.getMessage());
        }
    }
    
    @Override
    public void invalidate(String idempotencyKey) {
        redisTemplate.delete(idempotencyKey);
        log.debug("Invalidated Redis cache for key: {}", idempotencyKey);
    }
}

package io.orchestra.cloud.service;

import io.orchestra.core.service.LockProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-based implementation of LockProvider.
 * Provides distributed locking capabilities suitable for multi-instance production environments.
 * 
 * This implementation is automatically selected when Redis is available on the classpath,
 * replacing the in-memory implementation from orchestra-core.
 */
@Service
@Primary
@ConditionalOnClass(StringRedisTemplate.class)
@RequiredArgsConstructor
@Slf4j
public class RedisLockProvider implements LockProvider {
    
    private final StringRedisTemplate redisTemplate;
    
    @Override
    public boolean tryLock(String lockKey, String lockValue, Duration ttl) {
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, ttl);
        
        if (Boolean.TRUE.equals(lockAcquired)) {
            log.debug("Distributed lock acquired in Redis for key: {} with TTL: {}", lockKey, ttl);
            return true;
        }
        
        log.debug("Failed to acquire distributed lock in Redis for key: {}", lockKey);
        return false;
    }
    
    @Override
    public void unlock(String lockKey) {
        redisTemplate.delete(lockKey);
        log.debug("Distributed lock released in Redis for key: {}", lockKey);
    }
    
    @Override
    public boolean isLocked(String lockKey) {
        Boolean hasKey = redisTemplate.hasKey(lockKey);
        return Boolean.TRUE.equals(hasKey);
    }
}

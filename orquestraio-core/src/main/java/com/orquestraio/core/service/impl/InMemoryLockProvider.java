package com.orquestraio.core.service.impl;

import com.orquestraio.core.service.LockProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of LockProvider.
 * Suitable for development, testing, and single-instance deployments.
 * 
 * WARNING: This implementation does not provide distributed locking
 * and is not suitable for multi-instance production environments.
 */
@Service
@Slf4j
public class InMemoryLockProvider implements LockProvider {
    
    private final Map<String, LockEntry> locks = new ConcurrentHashMap<>();
    
    @Override
    public boolean tryLock(String lockKey, String lockValue, Duration ttl) {
        // Clean up expired lock if exists
        LockEntry existingLock = locks.get(lockKey);
        if (existingLock != null && existingLock.isExpired()) {
            locks.remove(lockKey);
            existingLock = null;
        }
        
        if (existingLock != null) {
            log.debug("Lock already exists for key: {}", lockKey);
            return false;
        }
        
        Instant expiresAt = Instant.now().plus(ttl);
        locks.put(lockKey, new LockEntry(lockValue, expiresAt));
        log.debug("Lock acquired for key: {} with TTL: {}", lockKey, ttl);
        return true;
    }
    
    @Override
    public void unlock(String lockKey) {
        locks.remove(lockKey);
        log.debug("Lock released for key: {}", lockKey);
    }
    
    @Override
    public boolean isLocked(String lockKey) {
        LockEntry entry = locks.get(lockKey);
        if (entry == null) {
            return false;
        }
        
        if (entry.isExpired()) {
            locks.remove(lockKey);
            return false;
        }
        
        return true;
    }
    
    /**
     * Internal lock entry structure.
     */
    private record LockEntry(String value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}

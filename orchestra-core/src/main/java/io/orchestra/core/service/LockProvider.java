package io.orchestra.core.service;

import java.time.Duration;

/**
 * Interface for distributed locking mechanism.
 * Provides lock acquisition and release capabilities to prevent
 * concurrent processing of the same operation.
 * 
 * Implementations can be in-memory (for single instance) or 
 * distributed (Redis, Zookeeper, etc.) for multi-instance environments.
 */
public interface LockProvider {
    
    /**
     * Attempts to acquire a lock for the given key.
     * 
     * @param lockKey unique identifier for the lock
     * @param lockValue value to associate with the lock (typically "LOCKED")
     * @param ttl time-to-live for the lock
     * @return true if lock was acquired, false if already locked
     * @throws IllegalStateException if lock acquisition fails
     */
    boolean tryLock(String lockKey, String lockValue, Duration ttl);
    
    /**
     * Releases the lock for the given key.
     * 
     * @param lockKey unique identifier for the lock
     */
    void unlock(String lockKey);
    
    /**
     * Checks if a lock exists for the given key.
     * 
     * @param lockKey unique identifier for the lock
     * @return true if lock exists, false otherwise
     */
    boolean isLocked(String lockKey);
}

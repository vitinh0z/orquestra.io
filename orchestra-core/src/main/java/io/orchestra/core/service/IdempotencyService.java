package io.orchestra.core.service;

import java.time.Duration;
import java.util.Optional;

/**
 * Interface for idempotency management.
 * Provides mechanisms to ensure payment operations are idempotent,
 * preventing duplicate processing of the same request.
 * 
 * Implementations can be in-memory (for development/testing) or 
 * distributed (Redis, etc.) for production environments.
 */
public interface IdempotencyService {
    
    /**
     * Checks if a cached response exists for the given idempotency key.
     * 
     * @param idempotencyKey unique identifier for the operation
     * @param responseType class type of the expected response
     * @param <T> response type
     * @return Optional containing cached response if found, empty otherwise
     */
    <T> Optional<T> getCachedResponse(String idempotencyKey, Class<T> responseType);
    
    /**
     * Stores a response in the cache with the given idempotency key.
     * 
     * @param idempotencyKey unique identifier for the operation
     * @param response the response object to cache
     * @param ttl time-to-live for the cached entry
     * @param <T> response type
     */
    <T> void cacheResponse(String idempotencyKey, T response, Duration ttl);
    
    /**
     * Removes a cached entry for the given idempotency key.
     * 
     * @param idempotencyKey unique identifier for the operation
     */
    void invalidate(String idempotencyKey);
}

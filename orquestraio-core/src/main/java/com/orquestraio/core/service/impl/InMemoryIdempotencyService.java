package com.orquestraio.core.service.impl;

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import com.orquestraio.core.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of IdempotencyService.
 * Suitable for development, testing, and single-instance deployments.
 * 
 * WARNING: This implementation does not persist data across application restarts
 * and is not suitable for distributed/multi-instance production environments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InMemoryIdempotencyService implements IdempotencyService {
    
    private final ObjectMapper objectMapper;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    @Override
    public <T> Optional<T> getCachedResponse(String idempotencyKey, Class<T> responseType) {
        CacheEntry entry = cache.get(idempotencyKey);
        
        if (entry == null) {
            return Optional.empty();
        }
        
        // Check if entry has expired
        if (entry.isExpired()) {
            cache.remove(idempotencyKey);
            log.debug("Cache entry expired for key: {}", idempotencyKey);
            return Optional.empty();
        }
        
        try {
            T response = objectMapper.readValue(entry.value(), responseType);
            log.info("Cache hit for idempotency key: {}", idempotencyKey);
            return Optional.of(response);
        } catch (DatabindException e) {
            log.error("Error deserializing cached response for key {}: {}", idempotencyKey, e.getMessage());
            cache.remove(idempotencyKey);
            return Optional.empty();
        }
    }
    
    @Override
    public <T> void cacheResponse(String idempotencyKey, T response, Duration ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(response);
            Instant expiresAt = Instant.now().plus(ttl);
            cache.put(idempotencyKey, new CacheEntry(jsonValue, expiresAt));
            log.debug("Cached response for idempotency key: {} with TTL: {}", idempotencyKey, ttl);
        } catch (DatabindException e) {
            log.error("Error serializing response for caching: {}", e.getMessage());
        }
    }
    
    @Override
    public void invalidate(String idempotencyKey) {
        cache.remove(idempotencyKey);
        log.debug("Invalidated cache for key: {}", idempotencyKey);
    }
    
    /**
     * Internal cache entry structure.
     */
    private record CacheEntry(String value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}

# Refactoring Example: ProcessPaymentUseCase

This document demonstrates the critical refactoring from direct Redis dependency to abstraction-based design.

## 📋 Before: Tight Coupling to Redis

```java
package io.orchestra.application.usecase;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {
    
    // ❌ PROBLEM: Direct dependency on Redis infrastructure
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    public PaymentResponseDTO execute(PaymentRequestDTO paymentRequest) {
        String idempotencyKey = "lock:payment:" + paymentRequest.idempotecyKey();
        
        try {
            // ❌ Direct Redis calls scattered throughout code
            acquirePaymentLock(idempotencyKey);
            
            Payment resultPayment = processPayment(gateway, apiKey, payment);
            
            return handleSuccessfulPayment(resultPayment, idempotencyKey);
            
        } catch (Exception e) {
            // ❌ Direct Redis deletion
            redisTemplate.delete(idempotencyKey);
            throw e;
        }
    }
    
    private void acquirePaymentLock(String idempotencyKey) {
        // ❌ Tight coupling: Can ONLY work with Redis
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(idempotencyKey, "LOCKED", Duration.ofHours(24));
            
        if (!Boolean.TRUE.equals(lockAcquired)) {
            throw new IllegalStateException("Pagamento já está em processamento.");
        }
    }
    
    private Optional<PaymentResponseDTO> checkIdempotency(PaymentRequestDTO payment) {
        String key = "lock:payment:" + payment.idempotecyKey();
        
        // ❌ Direct Redis access
        String cachedResponse = redisTemplate.opsForValue().get(key);
        
        if (cachedResponse != null) {
            if ("LOCKED".equals(cachedResponse)) {
                throw new IllegalStateException("Pagamento já está em processamento.");
            }
            try {
                return Optional.of(objectMapper.readValue(cachedResponse, PaymentResponseDTO.class));
            } catch (JsonProcessingException e) {
                log.error("Erro ao desserializar resposta do cache: {}", e.getMessage());
            }
        }
        return Optional.empty();
    }
    
    private PaymentResponseDTO handleSuccessfulPayment(Payment payment, String idempotencyKey) {
        Payment savedPayment = paymentPersistenceGateway.save(payment);
        PaymentResponseDTO paymentDto = paymentMapper.toDto(savedPayment);

        try {
            String jsonResponse = objectMapper.writeValueAsString(paymentDto);
            // ❌ Direct Redis caching
            redisTemplate.opsForValue().set(idempotencyKey, jsonResponse, Duration.ofHours(24));
        } catch (Exception e) {
            log.error("Falha ao cachear a resposta do pagamento: {}", e.getMessage());
        }
        return paymentDto;
    }
}
```

### Problems with This Approach:

1. ❌ **Can't work without Redis** - Requires Redis even for development/testing
2. ❌ **Hard to test** - Must mock StringRedisTemplate
3. ❌ **Not portable** - Can't switch to Memcached, Hazelcast, etc.
4. ❌ **Violates DIP** - High-level code depends on low-level infrastructure
5. ❌ **Core depends on Cloud** - Can't separate into modules

---

## ✅ After: Abstraction-Based Design

### Step 1: Define Abstractions (Orchestra Core)

```java
package io.orchestra.core.service;

import java.time.Duration;
import java.util.Optional;

/**
 * Interface for idempotency management.
 * Implementations can be in-memory or distributed.
 */
public interface IdempotencyService {
    <T> Optional<T> getCachedResponse(String idempotencyKey, Class<T> responseType);
    <T> void cacheResponse(String idempotencyKey, T response, Duration ttl);
    void invalidate(String idempotencyKey);
}
```

```java
package io.orchestra.core.service;

import java.time.Duration;

/**
 * Interface for distributed locking.
 * Implementations can be in-memory or distributed.
 */
public interface LockProvider {
    boolean tryLock(String lockKey, String lockValue, Duration ttl);
    void unlock(String lockKey);
    boolean isLocked(String lockKey);
}
```

### Step 2: In-Memory Implementation (Orchestra Core)

```java
package io.orchestra.core.service.impl;

import io.orchestra.core.service.IdempotencyService;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ✅ SOLUTION: In-memory implementation for dev/test
 * Works without any external dependencies!
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
        
        if (entry == null || entry.isExpired()) {
            cache.remove(idempotencyKey);
            return Optional.empty();
        }
        
        try {
            T response = objectMapper.readValue(entry.value(), responseType);
            log.info("Cache hit for idempotency key: {}", idempotencyKey);
            return Optional.of(response);
        } catch (DatabindException e) {
            log.error("Error deserializing cached response: {}", e.getMessage());
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
        } catch (DatabindException e) {
            log.error("Error serializing response for caching: {}", e.getMessage());
        }
    }
    
    @Override
    public void invalidate(String idempotencyKey) {
        cache.remove(idempotencyKey);
    }
    
    private record CacheEntry(String value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
```

```java
package io.orchestra.core.service.impl;

import io.orchestra.core.service.LockProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ✅ SOLUTION: In-memory lock provider
 * Thread-safe, works for single instances
 */
@Service
@Slf4j
public class InMemoryLockProvider implements LockProvider {
    
    private final Map<String, LockEntry> locks = new ConcurrentHashMap<>();
    
    @Override
    public boolean tryLock(String lockKey, String lockValue, Duration ttl) {
        LockEntry existingLock = locks.get(lockKey);
        
        if (existingLock != null && existingLock.isExpired()) {
            locks.remove(lockKey);
            existingLock = null;
        }
        
        if (existingLock != null) {
            return false;
        }
        
        Instant expiresAt = Instant.now().plus(ttl);
        locks.put(lockKey, new LockEntry(lockValue, expiresAt));
        return true;
    }
    
    @Override
    public void unlock(String lockKey) {
        locks.remove(lockKey);
    }
    
    @Override
    public boolean isLocked(String lockKey) {
        LockEntry entry = locks.get(lockKey);
        if (entry == null || entry.isExpired()) {
            locks.remove(lockKey);
            return false;
        }
        return true;
    }
    
    private record LockEntry(String value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
```

### Step 3: Redis Implementation (Orchestra Cloud)

```java
package io.orchestra.cloud.service;

import io.orchestra.core.service.IdempotencyService;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * ✅ SOLUTION: Redis implementation automatically selected when Redis is available
 * @Primary + @ConditionalOnClass ensure this replaces InMemoryIdempotencyService
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
        
        if (cachedValue == null || "LOCKED".equals(cachedValue)) {
            return Optional.empty();
        }
        
        try {
            T response = objectMapper.readValue(cachedValue, responseType);
            log.info("Redis cache hit for idempotency key: {}", idempotencyKey);
            return Optional.of(response);
        } catch (DatabindException e) {
            log.error("Error deserializing from Redis: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public <T> void cacheResponse(String idempotencyKey, T response, Duration ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(idempotencyKey, jsonValue, ttl);
        } catch (DatabindException e) {
            log.error("Error serializing response for Redis: {}", e.getMessage());
        }
    }
    
    @Override
    public void invalidate(String idempotencyKey) {
        redisTemplate.delete(idempotencyKey);
    }
}
```

```java
package io.orchestra.cloud.service;

import io.orchestra.core.service.LockProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * ✅ SOLUTION: Redis lock provider for distributed locking
 * Automatically replaces InMemoryLockProvider when Redis is available
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
        return Boolean.TRUE.equals(lockAcquired);
    }
    
    @Override
    public void unlock(String lockKey) {
        redisTemplate.delete(lockKey);
    }
    
    @Override
    public boolean isLocked(String lockKey) {
        Boolean hasKey = redisTemplate.hasKey(lockKey);
        return Boolean.TRUE.equals(hasKey);
    }
}
```

### Step 4: Refactored Use Case (Orchestra Cloud)

```java
package io.orchestra.cloud.application.usecase;

import io.orchestra.core.service.IdempotencyService;
import io.orchestra.core.service.LockProvider;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * ✅ SOLUTION: Use case now depends on ABSTRACTIONS
 * Works with BOTH in-memory and Redis implementations!
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentUseCase {
    
    // ✅ Abstraction: Can be in-memory OR Redis
    private final IdempotencyService idempotencyService;
    private final LockProvider lockProvider;
    
    // Other dependencies...
    private final PaymentPersistenceGateway paymentPersistenceGateway;
    private final GatewayRegistry gatewayRegistry;
    private final PaymentRequestDtoMapper paymentMapper;
    
    private static final String LOCK_PREFIX = "lock:payment:";
    
    public PaymentResponseDTO execute(PaymentRequestDTO paymentRequest) {
        // ✅ Check idempotency using abstraction
        Optional<PaymentResponseDTO> idempotentResponse = checkIdempotency(paymentRequest);
        if (idempotentResponse.isPresent()) {
            return idempotentResponse.get();
        }
        
        String idempotencyKey = LOCK_PREFIX + paymentRequest.idempotecyKey();
        Payment resultPayment = null;
        
        try {
            // ✅ Acquire lock using abstraction
            acquirePaymentLock(idempotencyKey);
            
            resultPayment = processPayment(gateway, apiKey, payment);
            
            return handleSuccessfulPayment(resultPayment, idempotencyKey);
            
        } catch (Exception e) {
            log.error("Falha ao processar pagamento: {}", e.getMessage());
            // ✅ Release lock using abstraction
            lockProvider.unlock(idempotencyKey);
            throw e;
        }
    }
    
    private Optional<PaymentResponseDTO> checkIdempotency(PaymentRequestDTO payment) {
        Optional<Payment> existingPayment = paymentPersistenceGateway
            .findByIdempotecyKey(payment.idempotecyKey());
            
        if (existingPayment.isPresent()) {
            return Optional.of(paymentMapper.toDto(existingPayment.get()));
        }

        String idempotencyKey = LOCK_PREFIX + payment.idempotecyKey();
        
        // ✅ Using abstraction - works with in-memory OR Redis!
        Optional<PaymentResponseDTO> cachedResponse = 
            idempotencyService.getCachedResponse(idempotencyKey, PaymentResponseDTO.class);
        
        if (cachedResponse.isPresent()) {
            return cachedResponse;
        }
        
        // ✅ Check lock status using abstraction
        if (lockProvider.isLocked(idempotencyKey)) {
            throw new IllegalStateException("Pagamento já está em processamento.");
        }
        
        return Optional.empty();
    }
    
    private void acquirePaymentLock(String idempotencyKey) {
        // ✅ Using abstraction - works with in-memory OR Redis!
        boolean lockAcquired = lockProvider.tryLock(idempotencyKey, "LOCKED", Duration.ofHours(24));
        
        if (!lockAcquired) {
            throw new IllegalStateException("Pagamento já está em processamento.");
        }
    }
    
    private PaymentResponseDTO handleSuccessfulPayment(Payment payment, String idempotencyKey) {
        Payment savedPayment = paymentPersistenceGateway.save(payment);
        PaymentResponseDTO paymentDto = paymentMapper.toDto(savedPayment);

        // ✅ Using abstraction - works with in-memory OR Redis!
        idempotencyService.cacheResponse(idempotencyKey, paymentDto, Duration.ofHours(24));
        
        return paymentDto;
    }
}
```

## 🎯 Benefits of the Refactoring

| Aspect | Before | After |
|--------|--------|-------|
| **Coupling** | ❌ Tight coupling to Redis | ✅ Depends on interfaces |
| **Testability** | ❌ Must mock Redis | ✅ Easy to mock/stub |
| **Portability** | ❌ Redis only | ✅ Any implementation |
| **Development** | ❌ Requires Redis setup | ✅ Works out-of-the-box |
| **Flexibility** | ❌ Hard to change | ✅ Easy to swap implementations |
| **Modularity** | ❌ Monolithic | ✅ Multi-module architecture |

## 🔄 How Auto-Selection Works

Spring Boot automatically chooses the right implementation:

### With Redis (Production)
```
✅ RedisIdempotencyService    (@Primary + Redis available)
✅ RedisLockProvider           (@Primary + Redis available)
```

### Without Redis (Development)
```
✅ InMemoryIdempotencyService  (Default from Core)
✅ InMemoryLockProvider        (Default from Core)
```

**No code changes needed!** The same `ProcessPaymentUseCase` works in BOTH scenarios.

---

**This refactoring demonstrates the power of the Dependency Inversion Principle and enables the "Open Core" architecture.**

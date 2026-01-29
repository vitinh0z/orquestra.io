# Orchestra Core - Payment Orchestration Engine

**Open Source** | **Lightweight** | **Pure Java 21**

## 📖 Overview

Orchestra Core is the lightweight, open-source heart of the Orchestra.io payment orchestration platform. It provides a minimal, dependency-free payment processing engine that can run anywhere - from development laptops to production servers.

## 🎯 Philosophy: "Open Core" Model

- **Pure Java 21**: Leverages Virtual Threads for high concurrency
- **Minimal Dependencies**: Only Spring Boot essentials (no Web, no Redis, no heavy infrastructure)
- **In-Memory First**: Works out-of-the-box without external services
- **Interface-Driven**: All infrastructure concerns abstracted behind interfaces

## 🏗️ Architecture

### Core Abstractions

#### IdempotencyService
Ensures payment operations are idempotent, preventing duplicate processing:
- `InMemoryIdempotencyService`: ConcurrentHashMap-based implementation for dev/test
- Extensible: Implement your own for Redis, Memcached, etc.

#### LockProvider  
Provides distributed locking to prevent concurrent payment processing:
- `InMemoryLockProvider`: Thread-safe in-memory locks for single instances
- Extensible: Implement for Redis, Zookeeper, etcd, etc.

### Domain Model

```
io.orchestra.core
├── domain
│   ├── entity          # Payment, Gateway, Tenant domain entities
│   ├── repository      # Repository interfaces (no implementations!)
│   └── service         # PaymentRouter interface
├── application
│   └── dto             # Data Transfer Objects
└── service
    ├── IdempotencyService    # Idempotency abstraction
    ├── LockProvider          # Lock abstraction
    └── impl                  # In-memory implementations
```

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>io.orchestra</groupId>
    <artifactId>orchestra-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Running Standalone

Orchestra Core can run completely standalone:

```java
@SpringBootApplication
public class MyPaymentApp {
    public static void main(String[] args) {
        SpringApplication.run(MyPaymentApp.class, args);
    }
}
```

The in-memory implementations will be auto-configured and ready to use!

## 📦 What's Included

### Dependencies
- `spring-boot-starter` - Minimal Spring Boot
- `spring-boot-starter-validation` - Bean validation
- `spring-boot-starter-json` - JSON processing (Jackson 3.x)
- `lombok` - Reduce boilerplate

### What's NOT Included
- ❌ No Web framework
- ❌ No Database/JPA
- ❌ No Redis
- ❌ No HTTP Client libraries
- ❌ No Cloud-specific dependencies

## 🔧 Extending Orchestra Core

### Custom Idempotency Implementation

```java
@Service
@Primary  // Overrides in-memory implementation
public class MyIdempotencyService implements IdempotencyService {
    @Override
    public <T> Optional<T> getCachedResponse(String key, Class<T> type) {
        // Your implementation (Memcached, Redis, etc.)
    }
    
    @Override
    public <T> void cacheResponse(String key, T response, Duration ttl) {
        // Your implementation
    }
    
    @Override
    public void invalidate(String key) {
        // Your implementation
    }
}
```

### Custom Lock Provider

```java
@Service
@Primary  // Overrides in-memory implementation
public class MyLockProvider implements LockProvider {
    @Override
    public boolean tryLock(String key, String value, Duration ttl) {
        // Your distributed lock implementation
    }
    
    @Override
    public void unlock(String key) {
        // Your implementation
    }
    
    @Override
    public boolean isLocked(String key) {
        // Your implementation
    }
}
```

## 🎓 Design Principles

1. **Dependency Inversion**: Core depends only on interfaces, never implementations
2. **Single Responsibility**: Each service has one clear purpose
3. **Open/Closed**: Open for extension, closed for modification
4. **Interface Segregation**: Small, focused interfaces
5. **Virtual Threads**: Designed for Java 21's Virtual Threads

## 📚 Use Cases

### Perfect For:
- ✅ Development and testing environments
- ✅ Single-instance deployments
- ✅ Learning payment orchestration concepts
- ✅ Building custom payment solutions
- ✅ Microservices that don't need distributed coordination

### Consider Orchestra Cloud For:
- 🏢 Multi-instance production deployments
- 🌍 Distributed systems requiring coordination
- 🔒 Enterprise security and compliance
- 📊 Advanced monitoring and observability
- ⚡ Redis-based high-performance caching

## 🤝 Contributing

Orchestra Core is open source! We welcome contributions:
- 🐛 Bug reports and fixes
- 💡 Feature suggestions
- 📖 Documentation improvements
- 🧪 Test coverage

## 📄 License

[Your License Here - e.g., Apache 2.0, MIT]

## 🔗 Related

- [Orchestra Cloud](../orchestra-cloud/README.md) - Enterprise edition with distributed features
- [Orchestra.io Documentation](https://orchestra.io/docs)

---

**Made with ❤️ by the Orchestra.io team**

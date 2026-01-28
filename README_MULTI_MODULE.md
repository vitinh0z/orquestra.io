# 🎵 Orchestra.io - Payment Orchestration Platform

**Multi-Module Maven** | **Open Core Model** | **Java 21**

## 📖 Overview

Orchestra.io is a modern payment orchestration platform following the "Open Core" business model. The project is split into two Maven modules:

- **[Orchestra Core](orchestra-core/)** - 🆓 Open Source lightweight payment engine
- **[Orchestra Cloud](orchestra-cloud/)** - 🏢 Enterprise distributed platform

## 🏗️ Architecture

```
orchestra-parent (Parent POM)
├── orchestra-core/          ← Open Source Core
│   ├── Domain entities
│   ├── Service interfaces
│   ├── In-memory implementations
│   └── Pure Java 21, minimal deps
│
└── orchestra-cloud/         ← Enterprise Cloud
    ├── Depends on: orchestra-core
    ├── Redis implementations
    ├── Multi-tenancy
    ├── Payment gateways
    └── Full Spring Boot stack
```

## 🎯 Design Principles

### Open Core Philosophy

1. **Core = Open Source**: Lightweight, minimal dependencies, works standalone
2. **Cloud = Enterprise**: Builds on Core, adds distributed features
3. **Interface-Driven**: Core defines interfaces, Cloud provides implementations
4. **Dependency Inversion**: Use case depends on abstractions, not implementations

### Key Abstractions

#### IdempotencyService
```java
// Core: InMemoryIdempotencyService (ConcurrentHashMap)
// Cloud: RedisIdempotencyService (@Primary when Redis available)
public interface IdempotencyService {
    <T> Optional<T> getCachedResponse(String key, Class<T> type);
    <T> void cacheResponse(String key, T response, Duration ttl);
    void invalidate(String key);
}
```

#### LockProvider
```java
// Core: InMemoryLockProvider (thread-safe in-memory)
// Cloud: RedisLockProvider (@Primary when Redis available)
public interface LockProvider {
    boolean tryLock(String key, String value, Duration ttl);
    void unlock(String key);
    boolean isLocked(String key);
}
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- (Optional) Redis for distributed features
- (Optional) PostgreSQL for production

### Build

```bash
# Build everything
mvn clean install

# Build only Core
cd orchestra-core && mvn clean install

# Build only Cloud (requires Core installed)
cd orchestra-cloud && mvn clean install
```

### Run

```bash
# Run Cloud module (includes Core)
cd orchestra-cloud
mvn spring-boot:run
```

Application starts at `http://localhost:8080`

## 📦 Module Overview

### Orchestra Core (Open Source)

**What's Inside:**
- ✅ Payment domain entities
- ✅ Service interfaces (IdempotencyService, LockProvider)
- ✅ In-memory implementations
- ✅ DTOs and validation
- ✅ Pure Java 21 with Virtual Threads

**Dependencies:**
- Spring Boot Starter (minimal)
- Spring Boot Starter Validation
- Spring Boot Starter JSON (Jackson 3.x)
- Lombok

**Use Cases:**
- Development and testing
- Single-instance deployments
- Learning and prototyping
- Building custom solutions

[📚 Read Core Documentation](orchestra-core/README.md)

### Orchestra Cloud (Enterprise)

**What's Added:**
- ✅ Redis-based distributed implementations
- ✅ Multi-tenant architecture
- ✅ Payment gateway integrations (Stripe, etc.)
- ✅ JPA persistence (PostgreSQL/H2)
- ✅ REST API with OpenAPI docs
- ✅ Security and authentication
- ✅ Monitoring with Actuator

**Additional Dependencies:**
- Orchestra Core
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Data Redis
- Spring Boot Starter Actuator
- Springdoc OpenAPI
- PostgreSQL, H2
- Stripe Java SDK

**Use Cases:**
- Production deployments
- Multi-instance horizontal scaling
- Enterprise SaaS platforms
- Mission-critical payment processing

[📚 Read Cloud Documentation](orchestra-cloud/README.md)

## 🔧 Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 (Virtual Threads) |
| Framework | Spring Boot 4.0.1 |
| Build Tool | Maven 3.9+ |
| JSON | Jackson 3.x (tools.jackson.*) |
| Caching | Redis (Cloud) / ConcurrentHashMap (Core) |
| Database | PostgreSQL (Cloud) / None (Core) |
| API Docs | Springdoc OpenAPI |
| Monitoring | Spring Boot Actuator |

## 🎨 Refactoring Highlights

### Before (Monolithic)
```java
@Service
public class ProcessPaymentUseCase {
    private final StringRedisTemplate redisTemplate;  // ❌ Tight coupling
    
    public PaymentResponseDTO execute(PaymentRequestDTO request) {
        // Direct Redis calls everywhere
        redisTemplate.opsForValue().setIfAbsent(key, "LOCKED");
    }
}
```

### After (Multi-Module)
```java
@Service
public class ProcessPaymentUseCase {
    private final IdempotencyService idempotencyService;  // ✅ Abstraction
    private final LockProvider lockProvider;              // ✅ Abstraction
    
    public PaymentResponseDTO execute(PaymentRequestDTO request) {
        // Works with ANY implementation!
        lockProvider.tryLock(key, "LOCKED", ttl);
    }
}
```

**Benefits:**
- ✅ Testable (mock implementations)
- ✅ Flexible (swap implementations)
- ✅ Portable (works with or without Redis)
- ✅ Clean (separation of concerns)

## 📊 Package Organization

```
Core Module:
io.orchestra.core
├── domain/          → Entities, constants
├── application/dto/ → Data Transfer Objects
└── service/         → Interfaces + in-memory implementations

Cloud Module:
io.orchestra.cloud
├── application/usecase/  → Use cases (ProcessPaymentUseCase)
├── service/              → Redis implementations
└── infra/                → Infrastructure (JPA, controllers, etc.)
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Test specific module
cd orchestra-core && mvn test
cd orchestra-cloud && mvn test

# Integration tests
mvn verify
```

## 📚 Documentation

- [Core Module README](orchestra-core/README.md)
- [Cloud Module README](orchestra-cloud/README.md)
- [API Documentation](http://localhost:8080/swagger-ui.html) (when running)
- [Actuator Endpoints](http://localhost:8080/actuator)

## 🤝 Contributing

We welcome contributions to Orchestra Core (open source module)!

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

- **Orchestra Core**: [Your Open Source License - e.g., Apache 2.0, MIT]
- **Orchestra Cloud**: Proprietary - Enterprise license required

## 🔗 Links

- 🌐 Website: https://orchestra.io
- 📧 Email: info@orchestra.io
- 🏢 Enterprise: sales@orchestra.io
- 💬 Community: [Discord/Slack Link]

## 👥 Team

Made with ❤️ by the Orchestra.io team

---

**Open Core Model** | **Java 21** | **Spring Boot 4.0.1** | **Production-Ready**

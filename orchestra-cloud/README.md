# Orchestra Cloud - Enterprise Payment Orchestration

**Enterprise** | **Distributed** | **Production-Ready**

## 📖 Overview

Orchestra Cloud is the enterprise-grade, production-ready edition of Orchestra.io. Built on top of [Orchestra Core](../orchestra-core/README.md), it adds distributed caching, multi-tenancy, advanced monitoring, and enterprise integrations for mission-critical payment processing at scale.

## 🎯 Enterprise Features

### Distributed Infrastructure
- **Redis Integration**: Distributed idempotency and locking across multiple instances
- **PostgreSQL**: Production-grade persistence with multi-tenant support
- **H2**: In-memory database for development/testing

### Payment Gateways
- **Stripe**: Full Stripe integration  
- **FakeGateway**: Testing and development mock gateway
- **Extensible**: Easy to add new payment providers

### Enterprise Capabilities
- **Multi-Tenancy**: Secure tenant isolation with encrypted credentials
- **API Key Authentication**: Secure tenant authentication
- **Actuator Monitoring**: Health checks, metrics, and observability
- **OpenAPI Documentation**: Auto-generated API documentation via Springdoc

## 🏗️ Architecture

Orchestra Cloud extends Orchestra Core with production-ready implementations:

```
io.orchestra.cloud
├── application
│   └── usecase              # ProcessPaymentUseCase (uses Core abstractions)
├── service
│   ├── RedisIdempotencyService  # @Primary Redis implementation
│   └── RedisLockProvider         # @Primary Redis implementation
├── infra
│   ├── config               # Spring configuration
│   ├── controller           # REST API controllers
│   ├── gateway              # Payment gateway implementations
│   ├── persistence          # JPA entities, repositories, mappers
│   ├── security             # API key filter, crypto service
│   └── tenant               # Multi-tenant context
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Redis (for distributed features)
- PostgreSQL (for production) or H2 (for development)

### Build

```bash
mvn clean install
```

### Run

```bash
cd orchestra-cloud
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Configuration

`src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/orchestra
spring.datasource.username=postgres
spring.datasource.password=postgres

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# API Documentation
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

## 📦 Dependencies

Orchestra Cloud includes everything from Core plus:

### Added Dependencies
- `orchestra-core` - Core payment engine
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-data-jpa` - Database access
- `spring-boot-starter-data-redis` - Distributed caching/locking
- `spring-boot-starter-actuator` - Monitoring
- `springdoc-openapi-starter-webmvc-ui` - API documentation
- `postgresql` - Production database
- `h2` - Development database
- `stripe-java` - Stripe integration

## 🎨 How It Works

### Automatic Implementation Selection

Orchestra Cloud uses Spring's `@Primary` and `@ConditionalOnClass` to automatically select the best implementation:

#### With Redis Available (Production)
```
✅ RedisIdempotencyService   (@Primary)
✅ RedisLockProvider          (@Primary)
```

#### Without Redis (Development)
```
✅ InMemoryIdempotencyService (from Core)
✅ InMemoryLockProvider       (from Core)
```

No code changes needed - it just works!

### ProcessPaymentUseCase - Refactored

**Before** (Monolithic):
```java
private final StringRedisTemplate redisTemplate;  // ❌ Tight coupling
```

**After** (Multi-module):
```java
private final IdempotencyService idempotencyService;  // ✅ Abstraction
private final LockProvider lockProvider;              // ✅ Abstraction
```

The same use case works with BOTH in-memory and Redis implementations!

## 🔐 Security

### Multi-Tenant Isolation
- Each tenant has isolated data
- Gateway credentials encrypted at rest
- Tenant context propagated through request lifecycle

### API Key Authentication
```bash
curl -H "X-API-Key: tenant-api-key-here" \
     -H "Content-Type: application/json" \
     -X POST http://localhost:8080/payments \
     -d '{ ... }'
```

## 📊 Monitoring

### Actuator Endpoints
- `/actuator/health` - Health check
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application info

### Execution History
All payment attempts logged for audit trail:
- Request/Response payloads
- Gateway selection
- Latency metrics
- Success/Failure status

## 🔌 Payment Gateway Integration

### Stripe Example

```java
@Component
public class StripeGateway implements PaymentGateway {
    
    @Override
    public String name() {
        return "STRIPE";
    }
    
    @Override
    public Payment process(Payment payment, String apiKey) {
        // Stripe processing logic
    }
}
```

Automatically discovered and registered via `GatewayRegistry`.

## 🎯 Production Deployment

### Docker

```dockerfile
FROM eclipse-temurin:21-jre
COPY target/orchestra-cloud-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/orchestra
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
```

## 🔧 Scaling

Orchestra Cloud is designed for horizontal scaling:

1. **Stateless**: No session state (all state in Redis/PostgreSQL)
2. **Distributed Locks**: Prevents concurrent payment processing
3. **Idempotency**: Redis-based deduplication across instances
4. **Database**: PostgreSQL handles concurrent writes

### Load Balancer Configuration
```
Instance 1 ←
Instance 2 ← Load Balancer ← Clients
Instance 3 ←
     ↓
   Redis (locks & cache)
     ↓
 PostgreSQL (persistence)
```

## 📚 API Documentation

Once running, visit:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## 🧪 Testing

```bash
# Unit tests
mvn test

# Integration tests (requires Redis & PostgreSQL)
mvn verify
```

## 🤝 Enterprise Support

For enterprise support, SLA, and custom features:
- 📧 Email: enterprise@orchestra.io
- 🌐 Website: https://orchestra.io/enterprise
- 📞 Phone: [Your phone]

## 📄 License

**Proprietary** - Enterprise license required for production use.

Contact sales for licensing: sales@orchestra.io

## 🔗 Related

- [Orchestra Core](../orchestra-core/README.md) - Open source core engine
- [Orchestra.io Documentation](https://orchestra.io/docs)
- [API Reference](https://orchestra.io/api)

---

**Enterprise-grade payment orchestration** | Orchestra.io

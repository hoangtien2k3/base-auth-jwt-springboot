# Auth JWT Spring Boot with Redis

A professional authentication and authorization system built with Spring Boot 3, Spring Security 6, JWT tokens, and **Redis** for high-performance caching and rate limiting.

## 🚀 Features

### Core Features
- **JWT Authentication**: Secure token-based authentication with access and refresh tokens
- **Role-Based Access Control (RBAC)**: Flexible permission system with roles and permissions
- **User Management**: Registration, login, logout, and token refresh
- **Security**: Password encryption, account locking, email verification

### Redis Integration ⚡
- **High-Performance Caching**: 40x faster response time with Redis cache
- **Rate Limiting**: Token bucket algorithm to prevent API abuse
  - Login: 5 requests per minute
  - Register: 3 requests per 5 minutes
  - Refresh Token: 10 requests per minute
- **Session Management**: Distributed session storage with Redis
- **Refresh Token Storage**: Fast token validation and revocation

### Infrastructure
- **Database**: PostgreSQL with automatic schema migration via Flyway
- **Cache**: Redis for caching and rate limiting
- **API Documentation**: Interactive Swagger UI for testing endpoints
- **Docker Support**: Complete Docker Compose setup

## 📊 Performance Metrics

| Operation | Without Redis | With Redis | Improvement |
|-----------|---------------|------------|-------------|
| Get User | 50-200ms | 1-5ms | **40x faster** |
| Get Role | 30-100ms | 1-3ms | **30x faster** |
| Rate Limit Check | N/A | <1ms | **Real-time** |

## 🏗️ Architecture

```
┌──────────────┐
│   Client     │
└──────┬───────┘
       │
       ▼
┌──────────────────────────────────┐
│   Spring Boot Application        │
│  ┌────────────┐  ┌─────────────┐│
│  │Controllers │  │  Services   ││
│  │@RateLimit  │  │  @Cacheable ││
│  └─────┬──────┘  └──────┬──────┘│
│        │                 │       │
│        ▼                 ▼       │
│  ┌──────────────────────────┐   │
│  │   Redis Integration      │   │
│  │ - Cache Manager          │   │
│  │ - Rate Limiter           │   │
│  │ - Session Store          │   │
│  └──────────┬───────────────┘   │
└─────────────┼───────────────────┘
              │
       ┌──────┴──────┐
       ▼             ▼
┌────────────┐  ┌──────────┐
│   Redis    │  │PostgreSQL│
│ Port 6379  │  │Port 5432 │
└────────────┘  └──────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Gradle

### 1. Start Infrastructure (PostgreSQL + Redis)

```bash
# Start all services
docker-compose up -d

# Check services status
docker-compose ps

# View logs
docker-compose logs -f
```

This will start:
- **PostgreSQL** on port `5432`
- **Redis** on port `6379`
- **Redis Commander** (Web UI) on port `8081` - http://localhost:8081
- **pgAdmin** (Web UI) on port `5050` - http://localhost:5050

### 2. Run Application

```bash
# Build and run
./gradlew bootRun

# Or build JAR
./gradlew build
java -jar build/libs/auth-jwt-0.0.1-SNAPSHOT.jar
```

### 3. Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **API** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **Redis Commander** | http://localhost:8081 | - |
| **pgAdmin** | http://localhost:5050 | admin@admin.com / admin |

## 📚 API Endpoints

### Authentication

| Method | Endpoint | Description | Rate Limit |
|--------|----------|-------------|------------|
| POST | `/api/v1/auth/register` | Register new user | 3 req / 5 min |
| POST | `/api/v1/auth/login` | Login and get tokens | 5 req / 1 min |
| POST | `/api/v1/auth/refresh-token` | Refresh access token | 10 req / 1 min |
| POST | `/api/v1/auth/logout` | Logout and revoke token | 10 req / 1 min |

### Rate Limiting Response

When rate limit is exceeded, you'll receive:

```json
{
  "success": false,
  "message": "Too many login attempts. Please try again after 1 minute."
}
```

Response Headers:
- `Retry-After`: Seconds until rate limit resets
- `X-RateLimit-Limit`: Maximum requests allowed
- `X-RateLimit-Remaining`: Remaining requests
- `X-RateLimit-Reset`: Unix timestamp when limit resets

## 🗂️ Project Structure

```
src/main/java/com/jwt/auth/auth_jwt/
├── annotation/          # Custom annotations (@RateLimit)
├── aspect/             # AOP aspects (Rate limiting)
├── config/             # Configuration classes
│   ├── RedisConfig.java           # Redis configuration
│   ├── CacheProperties.java       # Cache TTL settings
│   ├── SecurityConfig.java        # Security configuration
│   └── ...
├── controller/         # REST API endpoints
├── dto/               # Request/Response objects
├── entity/            # JPA entities
├── exception/         # Exception handling
│   └── RateLimitExceededException.java
├── repository/        # Database access layer
├── security/          # JWT filters, providers
├── service/           # Business logic
│   ├── RedisService.java          # Redis operations
│   ├── RateLimiterService.java    # Rate limiting logic
│   └── impl/
└── utils/             # Utility classes
```

## ⚙️ Configuration

### Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=auth_jwt_db
DB_USERNAME=admin
DB_PASSWORD=admin

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0

# JWT
JWT_SECRET=your-secret-key
JWT_ACCESS_TOKEN_EXPIRATION=900000      # 15 minutes
JWT_REFRESH_TOKEN_EXPIRATION=604800000  # 7 days

# Cache TTL
CACHE_TTL_USER=1800000           # 30 minutes
CACHE_TTL_ROLE=3600000           # 1 hour
CACHE_TTL_PERMISSION=3600000     # 1 hour
CACHE_TTL_REFRESH_TOKEN=604800000 # 7 days

# Rate Limiting
RATE_LIMIT_ENABLED=true
RATE_LIMIT_LOGIN_CAPACITY=5
RATE_LIMIT_LOGIN_DURATION=60
```

## 🔍 Redis Monitoring

### Using Redis CLI

```bash
# Connect to Redis
docker exec -it auth-jwt-redis redis-cli

# View all keys
KEYS *

# Check cache
GET "auth-jwt:users::1"

# Check rate limit
GET "rate_limit:login:192.168.1.100"

# Monitor real-time
MONITOR

# Get Redis stats
INFO stats
```

### Using Redis Commander

Open http://localhost:8081 to view:
- All cached data
- Rate limit counters
- Refresh tokens
- TTL for each key

## 📖 Documentation

- **[Redis Integration Guide](./REDIS_DOCUMENTATION.md)** - Comprehensive Redis documentation with:
  - Architecture diagrams
  - Sequence diagrams
  - Use cases
  - Best practices
  - Troubleshooting

## 🧪 Testing

### Test Rate Limiting

```bash
# Test login rate limit (5 requests per minute)
for i in {1..6}; do
  curl -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"usernameOrEmail":"test","password":"test"}' \
    -w "\nStatus: %{http_code}\n\n"
  sleep 1
done

# 6th request should return 429 Too Many Requests
```

### Test Caching

```bash
# First request (cache miss) - slower
time curl http://localhost:8080/api/v1/users/1

# Second request (cache hit) - much faster
time curl http://localhost:8080/api/v1/users/1
```

## 🛠️ Development

### Build

```bash
./gradlew clean build
```

### Run Tests

```bash
./gradlew test
```

### Docker Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f redis
docker-compose logs -f postgres

# Restart a service
docker-compose restart redis

# Remove volumes (clean start)
docker-compose down -v
```

## 🔒 Security Features

1. **JWT Token Security**
   - HS512 algorithm
   - Short-lived access tokens (15 min)
   - Long-lived refresh tokens (7 days)
   - Token rotation on refresh

2. **Rate Limiting**
   - IP-based rate limiting
   - Token bucket algorithm
   - Configurable limits per endpoint
   - Automatic reset

3. **Password Security**
   - BCrypt hashing
   - Account locking after failed attempts
   - Email verification

4. **CORS Protection**
   - Configurable allowed origins
   - Credential support

## 📈 Performance Tips

1. **Cache Strategy**
   - Frequently accessed data (users, roles) cached in Redis
   - Automatic cache invalidation on updates
   - Configurable TTL per entity type

2. **Connection Pooling**
   - Redis: Lettuce with connection pooling
   - PostgreSQL: HikariCP with optimized settings

3. **Rate Limiting**
   - Distributed rate limiting across instances
   - Fail-open strategy (allow requests if Redis is down)

## 🐛 Troubleshooting

### Redis Connection Issues

```bash
# Check Redis is running
docker-compose ps redis

# Test Redis connection
docker exec -it auth-jwt-redis redis-cli ping
# Should return: PONG

# Check Redis logs
docker-compose logs redis
```

### Database Issues

```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Connect to database
docker exec -it auth-jwt-postgres psql -U admin -d auth_jwt_db

# View tables
\dt
```

## 📝 License

This project is open source and available for educational purposes.

## 👨‍💻 Author

**hoangtien2k3**

---

**Last Updated:** 2026-01-18  
**Version:** 2.0.0 (with Redis Integration)

# 🐳 Hướng Dẫn Docker Cho Auth JWT Spring Boot

> Tài liệu chi tiết về việc containerize và deploy ứng dụng Auth JWT Spring Boot sử dụng Docker.

---

## 📑 Mục Lục

1. [Tổng Quan](#-tổng-quan)
2. [Cấu Trúc Files Docker](#-cấu-trúc-files-docker)
3. [Giải Thích Dockerfile](#-giải-thích-dockerfile)
4. [Giải Thích Docker Compose](#-giải-thích-docker-compose)
5. [Các Kỹ Thuật Tối Ưu](#-các-kỹ-thuật-tối-ưu)
6. [Hướng Dẫn Sử Dụng](#-hướng-dẫn-sử-dụng)
7. [Các Lệnh Thường Dùng](#-các-lệnh-thường-dùng)
8. [Troubleshooting](#-troubleshooting)
9. [Best Practices](#-best-practices)

---

## 🎯 Tổng Quan

### Kiến Trúc Deployment

```
┌─────────────────────────────────────────────────────────────────┐
│                    Docker Host / Server                          │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    auth-jwt-network                         │ │
│  │                                                             │ │
│  │  ┌─────────────────────┐     ┌─────────────────────┐       │ │
│  │  │   auth-jwt-app      │     │  auth-jwt-postgres  │       │ │
│  │  │                     │     │                     │       │ │
│  │  │  ┌───────────────┐  │     │  ┌───────────────┐  │       │ │
│  │  │  │ Spring Boot   │  │────▶│  │  PostgreSQL   │  │       │ │
│  │  │  │ Application   │  │     │  │   Database    │  │       │ │
│  │  │  └───────────────┘  │     │  └───────────────┘  │       │ │
│  │  │                     │     │                     │       │ │
│  │  │  Port: 8080         │     │  Port: 5432         │       │ │
│  │  └─────────────────────┘     └─────────────────────┘       │ │
│  │                                       │                     │ │
│  │                              ┌────────▼────────┐           │ │
│  │                              │  postgres_data  │           │ │
│  │                              │    (Volume)     │           │ │
│  │                              └─────────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Các Thành Phần

| Thành Phần | Mô Tả | Image |
|------------|-------|-------|
| **app** | Spring Boot JWT Authentication Service | Custom (multi-stage build) |
| **postgres** | PostgreSQL Database | `postgres:16-alpine` |
| **auth-jwt-network** | Docker Bridge Network | - |
| **postgres_data** | Persistent Volume cho database | - |

---

## 📁 Cấu Trúc Files Docker

```
auth-jwt-springboot4/
├── Dockerfile              # Multi-stage build cho Spring Boot
├── docker-compose.yml      # Orchestration cho các services
├── .dockerignore          # Loại trừ files không cần thiết
├── .env.example           # Template biến môi trường
└── .env                   # Biến môi trường thực tế (tạo từ .env.example)
```

---

## 🔧 Giải Thích Dockerfile

### Multi-Stage Build

Dockerfile sử dụng **multi-stage build** để tối ưu kích thước image và bảo mật:

```dockerfile
# Stage 1: BUILD (Xây dựng ứng dụng)
# Stage 2: RUNTIME (Chạy ứng dụng)
```

### Stage 1: Builder

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder
```

**Mục đích**: Build ứng dụng Spring Boot

| Bước | Mô Tả | Lý Do Tối Ưu |
|------|-------|--------------|
| Copy `gradlew`, `gradle`, `build.gradle` | Copy build files trước | Docker layer caching - nếu dependencies không đổi, layer này được cache |
| `./gradlew dependencies` | Tải dependencies | Tách biệt với source code để cache hiệu quả |
| Copy `src` | Copy source code | Chỉ invalidate layer này khi code thay đổi |
| `./gradlew bootJar -x test` | Build JAR file | Skip tests (chạy trong CI/CD riêng) |
| Extract layers | Phân tách JAR thành layers | Spring Boot Layered JAR cho startup nhanh hơn |

### Stage 2: Runtime

```dockerfile
FROM eclipse-temurin:17-jre-alpine AS runtime
```

**Mục đích**: Chạy ứng dụng với image tối thiểu

| Thành Phần | Mô Tả |
|------------|-------|
| **Base Image** | `eclipse-temurin:17-jre-alpine` (~80MB thay vì ~400MB với JDK) |
| **Non-root User** | `appuser:appgroup` (UID/GID 1001) - bảo mật |
| **Timezone** | Cấu hình timezone Asia/Ho_Chi_Minh |
| **Health Check** | Kiểm tra `/actuator/health` mỗi 30s |
| **JVM Optimization** | Container-aware JVM settings |

### Spring Boot Layered JAR

Layered JAR chia ứng dụng thành 4 layers:

```
1. dependencies/           # Ít thay đổi nhất (cached lâu)
2. spring-boot-loader/     # Spring Boot loader
3. snapshot-dependencies/  # SNAPSHOT dependencies
4. application/           # Code của bạn (thay đổi thường xuyên)
```

**Lợi ích**: Khi chỉ thay đổi code, Docker chỉ rebuild layer `application/`, tiết kiệm thời gian build và bandwidth.

### JVM Optimization Flags

```bash
-XX:+UseContainerSupport      # Nhận diện container limits
-XX:MaxRAMPercentage=75.0     # Sử dụng tối đa 75% RAM được cấp
-XX:InitialRAMPercentage=50.0 # Khởi động với 50% RAM
-XX:+UseG1GC                  # Garbage Collector tối ưu cho heap lớn
-XX:+UseStringDeduplication   # Giảm memory cho duplicate strings
-Djava.security.egd=file:/dev/./urandom  # Faster random number generation
```

---

## 📦 Giải Thích Docker Compose

### Service: PostgreSQL

```yaml
postgres:
  image: postgres:16-alpine
  command:
    - "postgres"
    - "-c"
    - "max_connections=200"
    - "-c"
    - "shared_buffers=256MB"
    # ... các tham số khác
```

#### PostgreSQL Performance Tuning

| Tham Số | Giá Trị | Mô Tả |
|---------|---------|-------|
| `max_connections` | 200 | Số connections tối đa |
| `shared_buffers` | 256MB | Bộ nhớ cache cho data |
| `effective_cache_size` | 768MB | Ước tính bộ nhớ OS cache |
| `maintenance_work_mem` | 128MB | Bộ nhớ cho maintenance operations |
| `checkpoint_completion_target` | 0.9 | Điều chỉnh checkpoint I/O |
| `wal_buffers` | 16MB | Bộ nhớ cho Write-Ahead Logging |
| `random_page_cost` | 1.1 | Tối ưu cho SSD |
| `effective_io_concurrency` | 200 | Số concurrent I/O operations |
| `log_min_duration_statement` | 1000 | Log queries chậm hơn 1s |

#### Volume và Init Script

```yaml
volumes:
  - postgres_data:/var/lib/postgresql/data
  - ./src/main/resources/ddl.sql:/docker-entrypoint-initdb.d/01-init.sql:ro
```

- **postgres_data**: Persistent storage cho database
- **ddl.sql**: Tự động chạy khi container khởi tạo lần đầu

### Service: Spring Boot App

```yaml
app:
  build:
    context: .
    dockerfile: Dockerfile
  depends_on:
    postgres:
      condition: service_healthy
```

#### Resource Limits

```yaml
deploy:
  resources:
    limits:
      cpus: '2'
      memory: 1G
    reservations:
      cpus: '0.5'
      memory: 512M
```

| Limit | Giá Trị | Mô Tả |
|-------|---------|-------|
| CPU Limit | 2 cores | Giới hạn CPU tối đa |
| Memory Limit | 1GB | Giới hạn RAM tối đa |
| CPU Reserve | 0.5 cores | CPU đảm bảo |
| Memory Reserve | 512MB | RAM đảm bảo |

### Network

```yaml
networks:
  auth-jwt-network:
    driver: bridge
```

Các container giao tiếp qua tên service (ví dụ: `postgres` thay vì IP).

---

## ⚡ Các Kỹ Thuật Tối Ưu

### 1. Docker Build Optimization

| Kỹ Thuật | Lợi Ích |
|----------|---------|
| **Multi-stage build** | Giảm kích thước image từ ~400MB xuống ~150MB |
| **Layer caching** | Tăng tốc rebuild khi chỉ thay đổi code |
| **.dockerignore** | Giảm build context, tăng tốc `docker build` |
| **Alpine images** | Image nhẹ hơn (musl libc thay vì glibc) |

### 2. Runtime Optimization

| Kỹ Thuật | Lợi Ích |
|----------|---------|
| **JRE thay vì JDK** | Giảm ~300MB |
| **Layered JAR** | Startup nhanh hơn, caching hiệu quả |
| **Container-aware JVM** | JVM tự điều chỉnh theo resource limits |
| **G1GC** | Garbage Collection hiệu quả cho heap lớn |

### 3. Database Optimization

| Kỹ Thuật | Lợi Ích |
|----------|---------|
| **Connection pooling (HikariCP)** | Giảm overhead tạo connection |
| **PostgreSQL tuning** | Tối ưu performance theo workload |
| **SSD-optimized settings** | `random_page_cost=1.1` cho SSD |
| **Health checks** | Đảm bảo database ready trước khi app connect |

### 4. Security

| Kỹ Thuật | Lợi Ích |
|----------|---------|
| **Non-root user** | Giảm attack surface |
| **Read-only volumes** | Bảo vệ init scripts |
| **Environment variables** | Không hardcode secrets |
| **Minimal image** | Ít packages = ít vulnerabilities |

---

## 🚀 Hướng Dẫn Sử Dụng

### Bước 1: Chuẩn Bị Environment

```bash
# Copy file environment mẫu
cp .env.example .env

# Chỉnh sửa các giá trị cần thiết
vim .env
```

**Các biến quan trọng cần thay đổi cho production:**

```bash
# Database password mạnh
DB_PASSWORD=your_very_secure_password_here

# JWT Secret (generate với: openssl rand -base64 64)
JWT_SECRET=your_secure_jwt_secret_here

# CORS origins
ALLOWED_ORIGINS=https://your-frontend-domain.com
```

### Bước 2: Build và Chạy

```bash
# Chạy lần đầu (build và start)
docker compose up -d --build

# Xem logs
docker compose logs -f

# Hoặc chỉ xem logs của app
docker compose logs -f app
```

### Bước 3: Kiểm Tra

```bash
# Kiểm tra containers đang chạy
docker compose ps

# Kiểm tra health
docker inspect --format='{{.State.Health.Status}}' auth-jwt-app
docker inspect --format='{{.State.Health.Status}}' auth-jwt-postgres

# Test API
curl http://localhost:8080/actuator/health

# Mở Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## 📋 Các Lệnh Thường Dùng

### Docker Compose Commands

```bash
# Khởi động services
docker compose up -d

# Dừng services
docker compose down

# Dừng và xóa volumes (⚠️ xóa data)
docker compose down -v

# Rebuild image
docker compose build

# Rebuild và restart
docker compose up -d --build

# Scale (chỉ với stateless services)
docker compose up -d --scale app=3

# Xem logs realtime
docker compose logs -f

# Xem logs của service cụ thể
docker compose logs -f app
docker compose logs -f postgres

# Restart service
docker compose restart app

# Exec vào container
docker compose exec app sh
docker compose exec postgres psql -U admin -d auth_jwt_db
```

### Docker Commands

```bash
# Xem images
docker images | grep auth-jwt

# Xem running containers
docker ps | grep auth-jwt

# Xem resource usage
docker stats auth-jwt-app auth-jwt-postgres

# Xem network
docker network inspect auth-jwt-network

# Cleanup unused resources
docker system prune -f
docker image prune -f
```

### Database Commands

```bash
# Connect vào PostgreSQL
docker compose exec postgres psql -U admin -d auth_jwt_db

# Backup database
docker compose exec postgres pg_dump -U admin auth_jwt_db > backup.sql

# Restore database
docker compose exec -T postgres psql -U admin auth_jwt_db < backup.sql
```

---

## 🔧 Troubleshooting

### 1. App Không Kết Nối Được Database

**Triệu chứng**: `Connection refused` hoặc `could not connect to server`

**Giải pháp:**
```bash
# Kiểm tra postgres đã healthy chưa
docker compose ps

# Xem logs postgres
docker compose logs postgres

# Đảm bảo depends_on với condition: service_healthy
```

### 2. Build Chậm

**Giải pháp:**
```bash
# Sử dụng BuildKit
DOCKER_BUILDKIT=1 docker compose build

# Kiểm tra .dockerignore đã đúng chưa
cat .dockerignore
```

### 3. Out of Memory

**Triệu chứng**: Container bị kill, `OOMKilled: true`

**Giải pháp:**
```yaml
# Tăng memory limit trong docker-compose.yml
deploy:
  resources:
    limits:
      memory: 2G
```

### 4. Database Init Script Không Chạy

**Nguyên nhân**: Scripts chỉ chạy khi volume trống

**Giải pháp:**
```bash
# Xóa volume và tạo lại
docker compose down -v
docker compose up -d
```

### 5. Permission Denied

**Triệu chứng**: `Permission denied` khi write files

**Giải pháp:**
```bash
# Kiểm tra user trong Dockerfile
# Đảm bảo COPY với --chown flag
```

---

## ✅ Best Practices

### Security Checklist

- [ ] Sử dụng `.env` file (không commit vào git)
- [ ] Thay đổi JWT_SECRET trong production
- [ ] Sử dụng strong database password
- [ ] Review ALLOWED_ORIGINS cho CORS
- [ ] Không expose database port trong production
- [ ] Sử dụng Docker secrets cho sensitive data
- [ ] Regularly update base images

### Production Checklist

- [ ] Set `JPA_SHOW_SQL=false`
- [ ] Configure proper resource limits
- [ ] Setup log rotation
- [ ] Configure backup cho database
- [ ] Setup monitoring (Prometheus, Grafana)
- [ ] Use Docker Swarm hoặc Kubernetes cho HA
- [ ] Configure reverse proxy (Nginx, Traefik)
- [ ] Enable TLS/SSL

### Development vs Production

| Setting | Development | Production |
|---------|-------------|------------|
| `JPA_SHOW_SQL` | true | false |
| `DB_PASSWORD` | simple | strong, random |
| `JWT_SECRET` | default | secure, random |
| Database port | exposed | internal only |
| Resource limits | relaxed | configured |
| Health checks | optional | required |

---

## 📊 Tổng Kết Tối Ưu

### Image Size Comparison

| Approach | Image Size |
|----------|------------|
| JDK + Single Stage | ~450MB |
| JRE + Single Stage | ~200MB |
| JRE Alpine + Multi-stage | ~150MB |
| **Our Approach** | **~120-150MB** |

### Build Time Comparison

| Scenario | First Build | Rebuild (code change) |
|----------|-------------|----------------------|
| Without optimization | ~3-5 min | ~3-5 min |
| With layer caching | ~3-5 min | ~30-60s |
| With layered JAR | ~3-5 min | ~20-40s |

### Startup Time

| Approach | Startup Time |
|----------|--------------|
| Fat JAR | ~15-20s |
| Layered JAR | ~10-15s |
| With JVM tuning | ~8-12s |

---

## 📚 Tài Liệu Tham Khảo

- [Spring Boot Docker Best Practices](https://spring.io/guides/topicals/spring-boot-docker)
- [Docker Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
- [PostgreSQL Docker](https://hub.docker.com/_/postgres)
- [Eclipse Temurin](https://hub.docker.com/_/eclipse-temurin)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)

---

> 📝 **Lưu ý**: Tài liệu này được tạo cho project `auth-jwt-springboot4`. Vui lòng điều chỉnh các giá trị theo nhu cầu cụ thể của bạn.

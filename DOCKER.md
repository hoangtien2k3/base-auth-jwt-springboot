# 🐳 Docker Setup

## Quick Start

```bash
# Build và chạy
docker compose up -d --build

# Xem logs
docker compose logs -f app

# Dừng
docker compose down
```

## Services

| Service | Port | Mô tả |
|---------|------|-------|
| **app** | 8080 | Spring Boot API |
| **postgres** | 5432 | PostgreSQL Database |

## URLs

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

## Architecture

```
┌─────────────────┐     ┌─────────────────┐
│   auth-jwt-app  │────▶│ auth-jwt-postgres│
│   (port 8080)   │     │   (port 5432)    │
└─────────────────┘     └─────────────────┘
```

## Dockerfile

Multi-stage build:
1. **Build stage**: Compile JAR với JDK Alpine
2. **Runtime stage**: Chạy với JRE Alpine (~150MB)

## Các Lệnh Thường Dùng

```bash
# Rebuild
docker compose up -d --build

# Vào PostgreSQL
docker compose exec postgres psql -U admin -d auth_jwt_db

# Xem resource usage
docker stats auth-jwt-app auth-jwt-postgres

# Xóa tất cả (kể cả data)
docker compose down -v
```

## Environment Variables

| Variable | Default | Mô tả |
|----------|---------|-------|
| `DB_HOST` | postgres | Database host |
| `DB_PORT` | 5432 | Database port |
| `DB_NAME` | auth_jwt_db | Database name |
| `DB_USERNAME` | admin | Database user |
| `DB_PASSWORD` | admin | Database password |

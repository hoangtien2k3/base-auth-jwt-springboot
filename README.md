# Auth JWT Spring Boot

A professional authentication and authorization system built with Spring Boot 3, Spring Security 6, and JWT tokens.

## Features

- **JWT Authentication**: Secure token-based authentication with access and refresh tokens
- **Role-Based Access Control (RBAC)**: Flexible permission system with roles and permissions
- **User Management**: Registration, login, logout, and token refresh
- **Security**: Password encryption, account locking, email verification
- **Database**: PostgreSQL with automatic schema migration via Flyway
- **API Documentation**: Interactive Swagger UI for testing endpoints

## Quick Start

### Setup

1. **Create Database**
   ```sql
   CREATE DATABASE auth_jwt_db;
   ```

2. **Configure Database** (Optional)
   
   Update `src/main/resources/application.yaml` if your credentials differ from defaults:
   ```yaml
   spring:
     datasource:
       username: admin
       password: admin
   ```

3. **Run Application**
   ```bash
   ./gradlew bootRun
   ```

4. **Access API Documentation**
   
   Open browser: `http://localhost:8080/swagger-ui.html`

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login and get tokens
- `POST /api/v1/auth/refresh-token` - Refresh access token
- `POST /api/v1/auth/logout` - Logout and revoke refresh token

## Project Structure

```
src/main/java/com/jwt/auth/auth_jwt/
├── config/          # Security, Swagger, and app configuration
├── controller/      # REST API endpoints
├── dto/            # Request/Response objects
├── entity/         # JPA entities (User, Role, Permission, RefreshToken)
├── exception/      # Global exception handling
├── repository/     # Database access layer
├── security/       # JWT filters, providers, and user details
├── service/        # Business logic
└── utils/          # Utility classes
```

## Configuration

Key configurations in `application.yaml`:

- **JWT Secret**: Configurable via `JWT_SECRET` environment variable
- **Token Expiration**: 
  - Access Token: 15 minutes (default)
  - Refresh Token: 7 days (default)
- **Database**: PostgreSQL connection settings
- **CORS**: Allowed origins for cross-origin requests

## License

This project is open source and available for educational purposes.

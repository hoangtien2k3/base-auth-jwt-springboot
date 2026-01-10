# Auth JWT Project

## 1. Giới thiệu
Dự án Authentication & Authorization chuyên nghiệp sử dụng Spring Boot 3, Spring Security 6, JWT, PostgreSQL.

## 2. Công nghệ sử dụng
- Java 17
- Spring Boot 3.2.1
- Spring Security 6
- JWT (Json Web Token)
- PostgreSQL
- Flyway Migration
- Swagger UI / OpenAPI 3

## 3. Cài đặt & Chạy

### Bước 1: Cấu hình Database
Tạo database PostgreSQL tên là `auth_jwt_db`:
```sql
CREATE DATABASE auth_jwt_db;
```

Cấu hình username/password trong `src/main/resources/application.yaml` nếu khác mặc định (`postgres`/`postgres`).

### Bước 2: Chạy ứng dụng
Mở terminal tại thư mục gốc dự án:
```bash
./gradlew bootRun
```

### Bước 3: Truy cập API Documentation
Mở trình duyệt và truy cập:
`http://localhost:8080/swagger-ui.html`

## 4. Account Default
Dữ liệu Roles và Permissions sẽ tự động được tạo khi ứng dụng khởi chạy lần đầu (nhờ Flyway).

## 5. Testing Flow
1. **Đăng ký**: `POST /api/v1/auth/register`
2. **Đăng nhập**: `POST /api/v1/auth/login` -> Nhận AccessToken & RefreshToken
3. **Sử dụng Token**: Copy AccessToken -> Bấm nút **Authorize** trên Swagger -> Paste Token
4. **Test Quyền**: Gọi các API trong `Test Controller`

## 6. Project Structure
- `config`: Cấu hình Security, Swagger, App beans.
- `controller`: API Endpoints.
- `dto`: Request/Response objects.
- `entity`: Database models.
- `repository`: Database access.
- `security`: JWT filter, provider, UserDetails custom.
- `service`: Business logic.
- `exception`: Global exception handling.

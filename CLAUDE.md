# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```powershell
# Build
mvnw.cmd clean install

# Run (local profile, port 8081)
mvnw.cmd spring-boot:run

# Run specific profile
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# Run all tests
mvnw.cmd test

# Run single test class
mvnw.cmd test -Dtest=MemberApplicationTests

# Package without tests
mvnw.cmd clean package -DskipTests
```

Swagger UI is available at `http://localhost:8081/swagger-ui.html` when running locally.

## Architecture

DDD-based layered architecture. Packages map directly to layers:

| Package | Responsibility |
|---|---|
| `interfaces/` | Controllers only — REST API (`api/v1/`) and Thymeleaf MVC (`web/`) |
| `application/` | DTOs, MapStruct mappers |
| `domain/` | Entities, service interfaces + `impl/`, MyBatis `@Mapper` interfaces |
| `infrastructure/` | DB config, Redis, security (JWT/OAuth2), mail, external APIs |
| `global/` | Cross-cutting: error handling, `ApiResponse<T>` wrapper, common utils |

## Key Conventions

**API Response:** All REST endpoints return `ApiResponse<T>` (`global/common/api/ApiResponse.java`). Use `ApiResponse.success(data)` for 200/201, `ApiResponse.failure(errorCode)` for errors.

**Error Handling:** Define new errors in `ErrorCode` enum, then throw `BusinessException(errorCode)` or a subclass from `global/error/exception/`. `GlobalExceptionHandler` catches everything.

**Dual Controller Pattern:** Each domain has two controllers — `interfaces/api/v1/{domain}/` for JSON REST and `interfaces/web/{domain}/` for Thymeleaf page rendering.

**DTO Mapping:** Use MapStruct via `application/{domain}/mapper/`. Never map manually between entity and DTO.

**MyBatis SQL:** XML mappers live in `src/main/resources/static/mybatis/mapper/**/*.xml`. The `@Mapper` interface is in `domain/{domain}/repository/mybatis/`.

## Profiles & Database

- **local** (default): MariaDB at `localhost:3306/SJSJSS`, Redis at `localhost:6379`, port 8081, SQL logging to stdout
- **dev**: Uses env vars `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- **prod**: Same env vars plus `REDIS_HOST`, `REDIS_PASSWORD`; port 80, logs to `/var/log/application.log`

## Dependencies Status

Spring Security and JWT (`jjwt-api`) are **commented out** in `pom.xml`. The `infrastructure/security/` package structure exists as a scaffold. Redis auto-configuration is excluded in `ProjectApplication`; manual config is expected in `infrastructure/db/redis/`.

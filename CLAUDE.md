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

# Build onpremise ZIP (B2B delivery)
mvnw.cmd clean package -Ponpremise
```

Swagger UI is available at `http://localhost:8081/swagger-ui.html` when running locally.

## Architecture

DDD-based layered architecture. Packages map directly to layers:

| Package | Responsibility |
|---|---|
| `interfaces/api/v1/` | **Primary surface** — REST API controllers (return `ApiResponse<T>`) |
| `interfaces/sample/web/` | Thymeleaf SSR sample pages (kept as learning examples, not core flow) |
| `application/` | Request/Response DTOs, MapStruct mappers |
| `domain/` | Entities, service interfaces + `impl/`, **JPA + MyBatis** repositories |
| `infrastructure/` | DB config, Redis, security (JWT/OAuth2), mail, external APIs (scaffold) |
| `global/` | Cross-cutting: error handling, `ApiResponse<T>` wrapper, common utils |

## Key Conventions

**API Response:** All REST endpoints return `ApiResponse<T>` (`global/common/api/ApiResponse.java`). Use `ApiResponse.ok(data)` for 200/201, throw a `BusinessException` subclass for errors.

**Error Handling:** Define new errors in `ErrorCode` enum, then throw `BusinessException(errorCode)` or a subclass from `global/error/exception/`. `GlobalExceptionHandler` catches everything.

**REST-only by default:** New domains add controllers under `interfaces/api/v1/{domain}/`. Thymeleaf pages are reserved for `interfaces/sample/web/` (intentionally separated so the main API surface stays clean).

**DTO Mapping:** Use MapStruct via `application/{domain}/mapper/`. Never map manually between entity and DTO.

**Data Access — JPA + MyBatis coexistence:**
- Default to **JPA** (`domain/{domain}/repository/jpa/{Domain}JpaRepository.java`) for basic CRUD, PK lookups, and simple derived queries.
- Use **MyBatis** (`domain/{domain}/repository/mybatis/{Domain}MybatisRepository.java` + XML in `src/main/resources/static/mybatis/mapper/`) only for dynamic WHERE, complex joins, or report queries.
- Entities carry both `@Entity` (JPA) and `@Alias` (MyBatis) so they work in either layer.

## Profiles & Database

- **local** (default): MariaDB at `localhost:3306/SJSJSS`, port 8081, SQL logging to stdout
- **dev**: Uses env vars `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- **prod**: Same env vars; port 80, logs to `/var/log/application.log`
- **test**: H2 in-memory DB with MariaDB mode, JPA `ddl-auto=create-drop`

JPA defaults are conservative — `ddl-auto=validate` and `open-in-view=false` in shared `application.yml`. The local profile overrides only `show-sql=true` for debugging.

## Deployment Topology

- **JAR build output**: `target/app.jar` (Maven `<finalName>app</finalName>`)
- **Containerization**: Multi-stage `Dockerfile` produces ~185MB JRE-alpine image
- **K8s deployment**: Helm Chart at `deploy/helm/spring-app/` (replaces raw manifests)
- **GitOps**: ArgoCD Application manifest at `deploy/argocd/application-prod.yaml` — Jenkins pushes `image.tag` to `values-prod.yaml`, ArgoCD auto-syncs
- **On-premise delivery**: ZIP package via `-Ponpremise` profile with Kafka-style `bin/` (Linux) + `bin/windows/` layout

## Jenkins Pipelines

- **Jenkinsfile** (main, per-commit): Build, test, SpotBugs, optional Docker build/push, then one of 7 `DEPLOY_METHOD` stages
- **Jenkinsfile.security** (nightly cron `H 2 * * *`): OWASP Dependency Check + Trivy full image scan — separated to keep main pipeline fast

## Dependencies Status

Spring Security and JWT (`jjwt-api`) are **commented out** in `pom.xml`. The `infrastructure/security/` package structure exists as a scaffold. Redis auto-configuration is excluded in `ProjectApplication`; manual config is expected in `infrastructure/db/redis/`.

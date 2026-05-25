# SpringStarterMavenProject

Spring Boot 3.4.2 기반의 DDD 레이어드 아키텍처 스타터 프로젝트.  
로컬 개발부터 SaaS(Docker/K8s), B2B 고객사 설치형까지 총 **7가지 배포 방식**을 Jenkins Pipeline으로 지원한다.

---

## 목차

1. [기술 스택](#1-기술-스택)
2. [프로젝트 구조](#2-프로젝트-구조)
3. [아키텍처](#3-아키텍처)
4. [로컬 개발 환경 설정](#4-로컬-개발-환경-설정)
5. [설정 파일 구조](#5-설정-파일-구조)
6. [빌드 & 테스트](#6-빌드--테스트)
7. [Jenkins 배포 파이프라인](#7-jenkins-배포-파이프라인)
8. [배포 방법별 상세 가이드](#8-배포-방법별-상세-가이드)
9. [B2B 고객사 설치형 배포](#9-b2b-고객사-설치형-배포)
10. [API 문서 (Swagger)](#10-api-문서-swagger)
11. [코드 컨벤션](#11-코드-컨벤션)

---

## 1. 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.2 |
| Build | Apache Maven (Maven Wrapper 포함) |
| Database | MariaDB 10.6+ (운영), H2 In-Memory (테스트) |
| ORM | MyBatis 3.0.3 |
| View | Thymeleaf + HTML/CSS/JS (Vanilla) |
| API 문서 | springdoc-openapi (Swagger UI) |
| 코드 생성 | Lombok, MapStruct |
| 보안 | Spring Security + JWT (scaffold 준비, 미활성화) |
| 캐시 | Redis (scaffold 준비, 미활성화) |
| CI/CD | Jenkins Declarative Pipeline |
| 컨테이너 | Docker, docker-compose |
| 오케스트레이션 | Kubernetes |
| 정적 분석 | SpotBugs + FindSecBugs, Checkstyle |
| 보안 스캔 | OWASP Dependency Check, Trivy |

---

## 2. 프로젝트 구조

```
SpringbootProject/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── ProjectApplication.java          # 앱 시작점
│   │   │   ├── interfaces/                      # ① 표현 계층
│   │   │   │   ├── api/v1/{domain}/             #   ★ REST API 컨트롤러 (메인 인터페이스)
│   │   │   │   ├── api/v2/                      #   향후 v2 API (scaffold)
│   │   │   │   └── sample/web/{domain}/         #   Thymeleaf SSR 샘플 (학습용 격리)
│   │   │   ├── application/                     # ② 응용 계층
│   │   │   │   └── {domain}/
│   │   │   │       ├── dto/request/             #   요청 DTO
│   │   │   │       ├── dto/response/            #   응답 DTO
│   │   │   │       └── mapper/                  #   MapStruct Entity↔DTO 변환
│   │   │   ├── domain/                          # ③ 도메인 계층
│   │   │   │   └── {domain}/
│   │   │   │       ├── entity/                  #   엔티티 (@Entity + @Alias 공존)
│   │   │   │       ├── service/                 #   서비스 인터페이스
│   │   │   │       ├── service/impl/            #   서비스 구현체
│   │   │   │       └── repository/
│   │   │   │           ├── jpa/                 #   ★ JpaRepository (기본 CRUD)
│   │   │   │           └── mybatis/             #   MyBatis (동적 SQL 전담)
│   │   │   ├── infrastructure/                  # ④ 인프라 계층
│   │   │   │   ├── db/mariadb/                  #   DB 설정 (scaffold)
│   │   │   │   ├── db/redis/                    #   Redis 설정 (scaffold)
│   │   │   │   ├── security/jwt/                #   JWT 보안 (scaffold)
│   │   │   │   ├── security/oauth2/             #   OAuth2 (scaffold)
│   │   │   │   ├── mail/                        #   메일 발송 (scaffold)
│   │   │   │   └── external/                    #   외부 API 연동 (scaffold)
│   │   │   └── global/                          # ⑤ 공통 횡단 관심사
│   │   │       ├── common/api/ApiResponse.java  #   표준 API 응답 래퍼
│   │   │       ├── error/ErrorCode.java         #   에러 코드 enum
│   │   │       ├── error/GlobalExceptionHandler #   전역 예외 처리
│   │   │       └── error/exception/             #   비즈니스 예외 클래스
│   │   └── resources/
│   │       ├── application.yml                  # 공통 설정 (JPA validate, OSIV off)
│   │       ├── application-local.yml            # 로컬 개발 환경 (JPA show-sql on)
│   │       ├── application-dev.yml              # 개발 서버
│   │       ├── application-prod.yml             # 운영 서버
│   │       ├── static/
│   │       │   ├── css/, js/                    # 정적 리소스
│   │       │   └── mybatis/mapper/**/*.xml      # MyBatis SQL 매퍼 (동적 쿼리만)
│   │       └── templates/                       # Thymeleaf HTML 템플릿 (샘플용)
│   └── test/
│       └── resources/application-test.yml       # 테스트 환경 (H2, ddl-auto=create-drop)
│
├── deploy/                                      # 배포 관련 설정 모음
│   ├── helm/spring-app/                         #   ★ Helm Chart (K8s 표준 패키징)
│   │   ├── Chart.yaml
│   │   ├── values.yaml                          #     기본값 (dev)
│   │   ├── values-prod.yaml                     #     운영 override
│   │   └── templates/                           #     Deployment / Service / ConfigMap / Secret
│   ├── argocd/                                  #   ★ ArgoCD GitOps Application 매니페스트
│   │   └── application-prod.yaml
│   └── onpremise/                               #   B2B 고객사 설치형 패키지 소스
│       ├── config/application.yml               #   고객사용 설정 템플릿
│       ├── bin/                                 #   Linux / macOS 스크립트 (.sh)
│       │   ├── install.sh / start.sh / stop.sh / uninstall.sh
│       │   └── windows/                         #   Windows 스크립트 (.bat + .ps1)
│       │       ├── install.bat / .ps1
│       │       ├── start.bat   / .ps1
│       │       ├── stop.bat    / .ps1
│       │       └── uninstall.bat / .ps1
│       ├── docker-compose.yml                   #   Docker 방식 고객사 설치용
│       └── INSTALL.md                           #   고객사 전달용 설치 가이드
│
├── config/                                      # 정적 분석 도구 설정
│   ├── owasp-suppressions.xml                   #   OWASP CVE 오탐 예외 목록
│   └── spotbugs-exclude.xml                     #   SpotBugs 오탐 예외 목록
│
├── Dockerfile                                   # 멀티스테이지 이미지 빌드 (루트 필수)
├── docker-compose.yml                           # 로컬 Docker 개발용 (루트 관례)
├── docker-compose.prod.yml                      # 운영 서버 docker-compose
├── Jenkinsfile                                  # 메인 CI/CD 파이프라인 (커밋마다 실행)
├── Jenkinsfile.security                         # ★ 야간 보안 풀스캔 (OWASP + Trivy, cron H 2 * * *)
├── pom.xml                                      # Maven 의존성 및 플러그인
├── .env.example                                 # 환경변수 템플릿 (Git 추적)
├── .editorconfig                                # 에디터 코드 스타일 통일
└── .gitignore
```

---

## 3. 아키텍처

### DDD 기반 레이어드 아키텍처

```
[HTTP 요청]
    ↓
interfaces/          ← Controller: 요청 수신, 응답 반환만 담당
    ↓
application/         ← DTO 변환(MapStruct), 유스케이스 조합
    ↓
domain/              ← 핵심 비즈니스 로직, Service, Entity, Repository 인터페이스
    ↓
infrastructure/      ← DB 연결, Redis, Security, 외부 API 등 기술 구현체
```

### API 응답 표준

모든 REST 엔드포인트는 `ApiResponse<T>`로 감싸서 반환한다.

```java
// 성공
return ApiResponse.success(data);          // 200/201

// 실패
throw new BusinessException(ErrorCode.XXX); // GlobalExceptionHandler가 처리
```

```json
// 성공 응답
{ "success": true, "data": { ... } }

// 실패 응답
{ "success": false, "error": { "code": "NOT_FOUND", "message": "..." } }
```

### 컨트롤러 구조 — REST API 중심 + Thymeleaf 샘플 격리

이 프로젝트의 **메인 인터페이스는 REST API** 이다. Thymeleaf 는 학습/예시 목적으로만 유지한다.

| 위치 | 역할 | 반환 타입 | 비고 |
|---|---|---|---|
| `interfaces/api/v1/{domain}/` | JSON REST API | `ApiResponse<T>` | 새 도메인은 여기에 추가 |
| `interfaces/sample/web/` | Thymeleaf SSR 데모 | `String` (뷰 이름) | 신규 도메인 추가 X (샘플 영역) |

새 기능 개발 시:
- API 컨트롤러는 `interfaces/api/v1/{domain}/{Domain}ApiController.java`
- 프론트엔드는 SPA(React/Vue) 별도 프로젝트가 이 REST API 를 호출하는 구조 권장

### 데이터 액세스 — JPA + MyBatis 공존

| 용도 | 사용 도구 | 위치 |
|---|---|---|
| 기본 CRUD, PK 조회, 단순 derived query | **JPA** (`JpaRepository`) | `domain/{domain}/repository/jpa/` |
| 동적 WHERE, 다중 조인, 통계/리포트 쿼리 | **MyBatis** (`@Mapper` + XML) | `domain/{domain}/repository/mybatis/` + `resources/static/mybatis/mapper/` |

엔티티는 `@Entity` (JPA) 와 `@Alias` (MyBatis) 를 동시에 가져 양쪽에서 모두 사용 가능.  
두 방식 모두 같은 DataSource / 트랜잭션 매니저를 공유하므로 한 트랜잭션 안에서 섞어 써도 안전.

```java
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberJpaRepository jpa;          // 기본 CRUD
    private final MemberMybatisRepository mybatis;  // 동적 검색

    public List<MemberResponseDto> findAll()           { return jpa.findAll()...; }       // ✅ JPA
    public List<MemberResponseDto> search(...request)  { return mybatis.search(...)...; } // ✅ MyBatis
}
```

---

## 4. 로컬 개발 환경 설정

### 사전 요구사항

- JDK 17 이상 설치 및 `JAVA_HOME` 설정
- MariaDB 10.6 이상 (기본 포트 `50002` 또는 `3306`)
- Git

### Step 1 — DB 초기화

MariaDB에 접속해 스키마와 계정을 생성한다.

```sql
CREATE DATABASE IF NOT EXISTS SJSJSS
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 앱 전용 계정 (root 사용 비권장)
CREATE USER IF NOT EXISTS 'your_db_user'@'%' IDENTIFIED BY 'your_db_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON SJSJSS.* TO 'your_db_user'@'%';
FLUSH PRIVILEGES;
```

### Step 2 — `.env` 파일 생성

`.env.example`을 복사해 `.env`를 만들고 실제 값으로 채운다.

```bash
copy .env.example .env      # Windows CMD
cp .env.example .env        # Mac/Linux
```

`.env` 파일 편집:

```dotenv
TAG=latest
DB_HOST=localhost
DB_PORT=50002          # 로컬 MariaDB 포트 (기본 3306이면 3306으로 변경)
DB_NAME=SJSJSS
DB_ROOT_PASSWORD=실제_루트_비밀번호
DB_USERNAME=실제_계정명
DB_PASSWORD=실제_비밀번호
```

> `.env`는 `.gitignore`에 등록되어 있어 Git에 커밋되지 않는다.

### Step 3 — 애플리케이션 실행

```powershell
# 기본 실행 (local 프로파일, 포트 8081)
mvnw.cmd spring-boot:run

# 특정 프로파일로 실행
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

실행 후 접속:
- 앱: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html

### IntelliJ IDEA에서 실행

`Run Configuration → Active profiles` 입력란에 `local` 입력.  
`.env` 파일 자동 로드: `EnvFile` 플러그인 설치 후 Run Configuration에서 `.env` 지정.

---

## 5. 설정 파일 구조

### 파일 역할 분리

| 파일 | 적용 환경 | 주요 내용 |
|---|---|---|
| `application.yml` | 전체 공통 | MyBatis, Swagger, 앱 이름, 공통 autoconfigure |
| `application-local.yml` | 로컬 개발 | MariaDB localhost, DevTools, SQL 콘솔 로그 |
| `application-dev.yml` | 개발 서버 | MariaDB 환경변수, 포트 8081 |
| `application-prod.yml` | 운영 서버 | MariaDB 환경변수, 포트 80, 파일 로그, Tomcat 튜닝 |
| `application-test.yml` | 테스트(Jenkins) | H2 인메모리 DB |
| `onpremise/config/application.yml` | B2B 고객사 설치형 | 평문 설정값, `file:./mapper/` 경로 |

### 프로파일 활성화 우선순위

```
1. 커맨드라인 인수:   java -jar app.jar --spring.profiles.active=prod
2. JVM 시스템 프로퍼티: java -Dspring.profiles.active=prod -jar app.jar
3. 환경변수:         SPRING_PROFILES_ACTIVE=prod
4. IDE:             IntelliJ → Run Configuration → Active profiles
5. 기본값:          application.yml의 spring.profiles.active: local
```

### 환경별 수정 필요 설정

#### 로컬 (`application-local.yml`)

```yaml
spring:
  datasource:
    url: jdbc:mariadb://${DB_HOST:localhost}:${DB_PORT:50002}/${DB_NAME:SJSJSS}
    # DB_HOST, DB_PORT, DB_NAME 기본값이 있어 .env 없어도 기본값으로 동작
    username: ${DB_USERNAME}    # .env 필수
    password: ${DB_PASSWORD}    # .env 필수
```

#### 운영 (`application-prod.yml`)

```yaml
# DB 정보: 환경변수로 주입 (Jenkins Credentials, K8s Secret, docker-compose env 등)
spring.datasource.url: jdbc:mariadb://${DB_HOST}:${DB_PORT:3306}/${DB_NAME}
logging.file.name: /var/log/application.log   # ← 서버 로그 경로
server.port: 80                                # ← 운영 포트
```

---

## 6. 빌드 & 테스트

```powershell
# 전체 빌드 + 테스트
mvnw.cmd clean install

# 테스트만 실행
mvnw.cmd test

# 특정 테스트 클래스만 실행
mvnw.cmd test -Dtest=MemberApplicationTests

# 테스트 없이 JAR 패키징
mvnw.cmd clean package -DskipTests

# 코드 스타일 검사
mvnw.cmd checkstyle:check

# 보안 취약점 스캔 (시간 소요)
mvnw.cmd org.owasp:dependency-check-maven:check

# SpotBugs 정적 분석
mvnw.cmd spotbugs:check
```

빌드 결과물: `target/SpringStarterMavenProject-1.0.jar`

---

## 7. Jenkins 배포 파이프라인

### 두 개의 파이프라인 — 빠른 빌드 + 야간 보안 스캔 분리

| 파이프라인 | 트리거 | 소요 시간 | 포함 작업 |
|---|---|---|---|
| **`Jenkinsfile`** (메인) | 커밋 / 수동 | 2~5분 | 빌드 + 테스트 + SpotBugs + Docker + 배포 |
| **`Jenkinsfile.security`** (보안) | cron `H 2 * * *` (매일 새벽 2시) | 20~40분 | OWASP Dependency Check + Trivy 풀스캔 |

OWASP 스캔은 NVD 데이터베이스 매칭에 시간이 오래 걸려, 매 커밋마다 돌리면 CI 가 느려진다. → 야간 1회로 분리.

### 메인 파이프라인 구조

```
① Checkout
② Build & Test         (mvn clean package → target/app.jar, JUnit 리포트)
③ Code Quality         (Checkstyle)
④ SpotBugs Security    (소스 정적 분석 — 1분 내 완료)
⑤ Docker Build         (Docker 기반 방식만)
⑥ Image Security Scan  (Trivy 빠른 스캔 — Docker 기반 방식만)
⑦ Docker Hub Push      (server-docker / server-k8s 만)
⑧ Deploy               (선택한 DEPLOY_METHOD 스테이지 1개만 실행)
```

### Jenkins 에서 실행하는 방법

**메인 파이프라인**:
1. Jenkins 파이프라인 Job 생성 → Pipeline script from SCM, `Jenkinsfile` 지정
2. **Build with Parameters** 클릭
3. `DEPLOY_METHOD` 드롭다운에서 원하는 배포 방법 선택
4. Build 시작

**보안 파이프라인** (별도 Job):
1. 신규 Pipeline Job 생성 → Pipeline script from SCM, `Jenkinsfile.security` 지정
2. 저장 후 자동 cron 트리거 활성화 (매일 새벽 2시)

---

## 8. 배포 방법별 상세 가이드

### 배포 방법 선택 기준

| 단계 | DEPLOY_METHOD | 상황 |
|---|---|---|
| **고객사 납품** | `package-zip` | JAR + 설정 + 스크립트를 ZIP으로 패키징 |
| **고객사 납품** | `package-docker` | Docker 이미지를 tar.gz로 저장해 ZIP으로 패키징 (폐쇄망) |
| **① 전통** | `server-jar` | 원격 서버에 JAR만 SSH 배포 (가장 단순) |
| **② 전통+패키지** | `server-jar-zip` | 원격 서버에 전체 패키지(JAR+설정+스크립트) SSH 배포 ★ |
| **③ 전통+무중단** | `server-blue-green` | Blue/Green 배포 — Nginx 트래픽 순간 전환, 자동 롤백 ★ |
| **④ 컨테이너** | `server-docker` | Docker Hub → 원격 서버 docker-compose 배포 ★ |
| **⑤ 오케스트레이션** | `server-k8s` | Docker Hub → Kubernetes 클러스터 롤링 배포 ★ |

---

### `server-jar` — 원격 서버에 JAR 배포

빌드된 JAR를 원격 Linux 서버로 전송해 자동 실행한다.

**Jenkinsfile 수정 필요**

```groovy
// Jenkinsfile — environment 블록
REMOTE_HOST = '실제_서버_IP'        // ← 변경
REMOTE_USER = 'ubuntu'              // ← 서버 계정으로 변경
REMOTE_PATH = '/home/ubuntu/app'    // ← 배포 경로로 변경
```

**Jenkins Credentials 등록**
```
Jenkins 관리 → Credentials → Global → Add Credentials
  Kind    : SSH Username with private key
  ID      : deploy-server-ssh
  Username: 서버 계정 (예: ubuntu)
  Key     : 서버 접속용 PEM 키 내용 붙여넣기
```

**결과물**: 원격 서버 `서버IP:8080`에서 앱 구동

---

### `server-jar-zip` ★ — 온프레미스 패키지 원격 서버 자동 배포

온프레미스 ZIP 패키지(JAR + 설정 + 스크립트)를 SSH로 원격 서버에 전송 후 자동 설치/업데이트.  
**B2B 고객사 서버를 CI/CD로 직접 자동 배포할 때 사용.**

**특징**
- 최초 설치: `config/application.yml` 복사 후 수동 DB 설정 안내 (앱 미시작)
- 재배포: `app.jar` + `bin/` + `mapper/`만 교체, **기존 DB 설정 보존**

**Jenkinsfile 수정 필요**

```groovy
REMOTE_HOST = '실제_서버_IP'
REMOTE_USER = 'ubuntu'
REMOTE_PATH = '/home/ubuntu/app'    // 설치 경로 (current/ 하위에 배치됨)
```

**Jenkins Credentials 등록**
```
Kind    : SSH Username with private key
ID      : deploy-server-ssh
```

**결과물**: `REMOTE_PATH/current/`에 설치 완료, 원격 서버에서 앱 구동

---

### `server-blue-green` ★ — 무중단 Blue/Green 배포

Blue(8081)와 Green(8082) 두 인스턴스를 번갈아 배포하고 Nginx가 트래픽을 순간 전환한다.  
**서비스 중단 0초. 헬스체크 실패 시 자동 롤백.**

**동작 원리**
```
① 현재 활성 인스턴스 확인 (Blue 또는 Green PID 파일로 판단)
② 비활성 인스턴스에 신규 JAR 배포 (반대 포트에서 기동)
③ 헬스체크 60초 대기 (/actuator/health HTTP 200 확인)
④ (성공) Nginx upstream 순간 전환 → 구 인스턴스 Graceful Shutdown
   (실패) 신규 인스턴스 즉시 제거 → 구 버전 자동 유지 (롤백)
```

**서버 사전 설정**

```bash
# 1. Nginx 설치 및 설정 파일 생성
sudo apt install -y nginx
sudo tee /etc/nginx/sites-enabled/spring-app <<'EOF'
upstream spring_app { server 127.0.0.1:8081; }
server {
    listen 80;
    location / { proxy_pass http://spring_app; }
}
EOF
sudo nginx -t && sudo nginx -s reload

# 2. nginx reload를 위한 sudo 권한 부여 (비밀번호 없이)
echo "ubuntu ALL=(ALL) NOPASSWD: /usr/sbin/nginx" | sudo tee /etc/sudoers.d/nginx
```

**Jenkinsfile 수정 필요**

```groovy
REMOTE_HOST = '실제_서버_IP'
REMOTE_USER = 'ubuntu'
REMOTE_PATH = '/home/ubuntu/app'
BLUE_PORT   = '8081'    // 기본값 유지 가능
GREEN_PORT  = '8082'    // 기본값 유지 가능
```

**Jenkins Credentials 등록**
```
Kind    : SSH Username with private key
ID      : deploy-server-ssh
```

**결과물**: 서비스 중단 0초, Nginx를 통해 신규 버전으로 교체 완료

---

### `server-docker` ★ — Docker Hub + 원격 서버 docker-compose

이미지를 Docker Hub에 Push하고 원격 서버에서 `docker-compose`로 실행한다.  
**중소규모 SaaS 서비스의 현업 표준.**

**Jenkinsfile 수정 필요**

```groovy
DOCKER_IMAGE = "실제_도커허브_ID/spring-starter-maven"  // ← 변경
REMOTE_HOST  = '실제_서버_IP'                           // ← 변경
REMOTE_USER  = 'ubuntu'                                 // ← 변경
REMOTE_PATH  = '/home/ubuntu/app'                       // ← 변경
```

**Jenkins Credentials 등록 (2개)**
```
① dockerhub-credentials
   Kind    : Username with password
   ID      : dockerhub-credentials
   Username: Docker Hub 아이디
   Password: Docker Hub 비밀번호 또는 Access Token

② deploy-server-ssh
   Kind    : SSH Username with private key
   ID      : deploy-server-ssh
```

**원격 서버 사전 조건**
```bash
docker --version        # 20.10+
docker-compose --version  # 또는 docker compose version
```

**결과물**: 원격 서버에서 컨테이너 구동

---

### `server-k8s` ★ — Kubernetes 롤링 배포 (Helm + ArgoCD GitOps)

Docker 이미지를 레지스트리에 Push 한 뒤, **Jenkins 는 클러스터를 직접 건드리지 않는다**.  
Helm Chart 의 `values-prod.yaml` 에서 `image.tag` 만 신규 빌드 번호로 교체해 Git 에 커밋·푸시 →  
클러스터에 설치된 **ArgoCD 가 Git 변경을 감지해 자동 sync**.

**워크플로우**

```
┌─ Jenkins ─┐         ┌─ Git Repo ─┐         ┌─ ArgoCD ─┐         ┌─ K8s ─┐
│ 빌드 +    │  push   │ Helm Chart │ poll    │ 변경 감지 │  apply  │ 배포   │
│ image.tag │ ──────► │ values.yaml│ ──────► │ + diff    │ ──────► │ 완료   │
└───────────┘         └────────────┘         └───────────┘         └────────┘
```

**Jenkinsfile 수정 필요**

```groovy
DOCKER_IMAGE = "실제_도커허브_ID/spring-starter-maven"
GIT_REPO_URL = 'https://github.com/실제_조직/SpringbootProject.git'
GIT_BRANCH   = 'main'
```

**Helm values 수정 필요** (`deploy/helm/spring-app/values-prod.yaml`)

```yaml
image:
  repository: 실제_도커허브_ID/spring-starter-maven
  # tag 는 Jenkins 가 빌드 번호로 자동 교체

config:
  SPRING_PROFILES_ACTIVE: "prod"
  DB_HOST: "prod-db.internal"      # ← 운영 DB 호스트
  DB_PORT: "3306"
  DB_NAME: "SJSJSS"

# secret 은 Git 평문 커밋 금지 — Sealed Secrets / External Secrets / Vault 사용 권장
secret:
  DB_USERNAME: "CHANGE_ME"
  DB_PASSWORD: "CHANGE_ME"
```

**ArgoCD Application 등록** (`deploy/argocd/application-prod.yaml`) — 최초 1회만

```bash
# deploy/argocd/application-prod.yaml 안의 repoURL 을 실제 저장소로 수정 후
kubectl apply -f deploy/argocd/application-prod.yaml
```

**Jenkins Credentials 등록 (2개)**
```
① dockerhub-credentials (위와 동일)

② git-push-token
   Kind    : Username with password
   ID      : git-push-token
   Username: GitHub 사용자명
   Password: Personal Access Token (repo 쓰기 권한)
```

**장점**
- Jenkins 가 클러스터 자격 증명을 보관할 필요 없음 (kubeconfig 불필요)
- Git 이 단일 진실 공급원 — 누가 언제 무엇을 배포했는지 git log 로 추적
- ArgoCD `selfHeal` 옵션으로 클러스터에서 누가 수동 수정해도 자동 복구

**결과물**: K8s 클러스터에서 롤링 업데이트, `LoadBalancer IP:80` 접속  
ArgoCD UI 또는 `argocd app get spring-app-prod` 로 sync 상태 확인

---

### `package-zip` — B2B 고객사 설치형 JAR 패키지

JAR + 설정 파일 + SQL 매퍼 + 시작/종료 스크립트를 ZIP으로 패키징한다.  
**JDK 17만 있으면 설치 가능. 인터넷 불필요.**

**수정할 설정 없음** — 빌드만 하면 즉시 생성.

**생성 파일**: `spring-starter-maven-1.0-release.zip`

```
spring-starter-maven-1.0/            (Apache Kafka 관례 — Linux: bin/, Windows: bin/windows/)
├── app.jar
├── config/
│   └── application.yml     ← 고객이 DB 정보 입력
├── mapper/
│   └── *.xml               ← SQL 수정 시 편집
├── bin/                    ← Linux / macOS
│   ├── install.sh          ← 자동 설치 (DB 설정 + 서비스 등록)
│   ├── start.sh / stop.sh / uninstall.sh
│   └── windows/            ← Windows 전용
│       ├── install.bat     ← 자동 설치 (DB 설정 + 앱 시작)
│       ├── start.bat / stop.bat / uninstall.bat
│       └── (*.ps1 — 각 .bat의 실제 로직 포함)
└── INSTALL.md
```

**고객사 설치 흐름**

```bash
# Linux — 자동 설치
unzip spring-starter-maven-1.0-release.zip
cd spring-starter-maven-1.0
bin/install.sh    # DB 정보 입력 → 설정 자동화 → 앱 시작

# Windows — 자동 설치
# 탐색기로 ZIP 해제 → bin\windows\install.bat 더블클릭 → DB 정보 입력
```

자세한 내용 → [deploy/onpremise/INSTALL.md](deploy/onpremise/INSTALL.md)

---

### `package-docker` — B2B 고객사 설치형 Docker 패키지

Docker 이미지를 `tar.gz`로 저장하고 `docker-compose`와 함께 ZIP으로 패키징한다.  
**인터넷 없는 폐쇄망 환경에서도 Docker만 있으면 설치 가능.**

**Jenkinsfile 수정 필요**
```groovy
DOCKER_IMAGE = "실제_도커허브_ID/spring-starter-maven"  // 이미지 이름 기준으로 tar.gz 생성
```

**생성 파일**: `spring-starter-maven-1.0-docker-release.zip`

```
spring-starter-maven-1.0-docker/
├── image.tar.gz           ← Docker 이미지 (docker load로 설치)
├── docker-compose.yml     ← DB 정보 입력 후 실행
├── INSTALL.md
└── load-and-run.sh        ← 원클릭 설치 스크립트 (Linux)
```

**고객사 설치 흐름 (Linux)**

```bash
unzip spring-starter-maven-1.0-docker-release.zip
cd spring-starter-maven-1.0-docker
vi docker-compose.yml      # DB_HOST, DB_USERNAME, DB_PASSWORD 수정
chmod +x load-and-run.sh
./load-and-run.sh          # 이미지 로드 + 컨테이너 시작
```

**고객사 설치 흐름 (Windows)**

```powershell
Expand-Archive *.zip
notepad docker-compose.yml    # DB 정보 수정
docker load -i image.tar.gz
docker-compose up -d
```

자세한 내용 → [deploy/onpremise/INSTALL.md](deploy/onpremise/INSTALL.md)

---

## 9. B2B 고객사 설치형 배포

### 고객사 설정 파일 (`deploy/onpremise/config/application.yml`)

고객사에 전달되는 설정 파일. **Spring Boot 외부 설정 원칙**에 따라 `app.jar` 옆  
`config/` 폴더에 위치하면 JAR 내부 설정보다 자동으로 우선 적용된다.

고객이 수정해야 할 항목:

```yaml
# deploy/onpremise/config/application.yml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/SJSJSS   # DB 서버 IP, 포트, DB명 변경
    username: your_db_user                       # DB 계정명 변경
    password: your_db_password                   # DB 비밀번호 변경

server:
  port: 8080    # 사용할 포트 변경

mybatis:
  mapper-locations: file:./mapper/**/*.xml       # 외부 mapper 디렉터리 (수정 불필요)
```

> `file:./mapper/` 경로를 사용하기 때문에 고객사에서 SQL 매퍼 XML을  
> 직접 수정하고 앱만 재시작하면 반영된다 (JAR 재빌드 불필요).

### Linux 서비스 자동 시작 (systemd)

`bin/install.sh`를 root로 실행하면 systemd 등록을 대화형으로 안내해 준다.  
수동으로 등록하려면:

```bash
sudo tee /etc/systemd/system/spring-app.service <<EOF
[Unit]
Description=Spring Starter Maven Application
After=network.target

[Service]
Type=simple
WorkingDirectory=/opt/spring-starter-maven
ExecStart=/opt/spring-starter-maven/bin/start.sh
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl enable --now spring-app
```

---

## 10. API 문서 (Swagger)

앱 실행 후 아래 URL에서 API를 확인하고 테스트할 수 있다.

| URL | 설명 |
|---|---|
| http://localhost:8081/swagger-ui.html | Swagger UI |
| http://localhost:8081/api-docs | OpenAPI JSON 스펙 |

Postman import: `http://localhost:8081/api-docs` URL을 Postman의 Import에 붙여넣기.

---

## 11. 코드 컨벤션

### 새 도메인 추가 시 체크리스트

```
□ interfaces/api/v1/{domain}/     — REST 컨트롤러
□ interfaces/web/{domain}/        — Thymeleaf 컨트롤러
□ application/{domain}/dto/       — 요청/응답 DTO
□ application/{domain}/mapper/    — MapStruct 매퍼
□ domain/{domain}/entity/         — 엔티티
□ domain/{domain}/service/        — 서비스 인터페이스
□ domain/{domain}/service/impl/   — 서비스 구현체
□ domain/{domain}/repository/mybatis/ — @Mapper 인터페이스
□ resources/static/mybatis/mapper/{domain}/*.xml — SQL 매퍼
```

### 에러 추가 방법

```java
// 1. ErrorCode enum에 추가
MEMBER_NOT_FOUND(404, "MEMBER_001", "회원을 찾을 수 없습니다"),

// 2. Service에서 throw
throw new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND);

// 3. GlobalExceptionHandler가 자동으로 ApiResponse로 변환해 응답
```

### DTO 변환 규칙

```java
// ✅ MapStruct 사용 (application/{domain}/mapper/)
MemberResponseDto dto = memberMapper.toDto(member);

// ❌ 수동 변환 금지
MemberResponseDto dto = new MemberResponseDto(member.getId(), ...);
```

### 환경 분리 원칙

```
✅ 환경변수(${VAR}) 또는 외부 설정 파일로 주입
❌ application.yml에 DB 비밀번호, API 키 등 민감 정보 직접 작성
```

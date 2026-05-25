# SpringStarterMavenProject

Spring Boot 3.4.2 기반의 DDD 레이어드 아키텍처 스타터 프로젝트.  
로컬 개발부터 SaaS(Docker/K8s), B2B 고객사 설치형까지 총 **8가지 배포 방식**을 Jenkins Pipeline으로 지원한다.

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
│   │   │   │   ├── api/v1/{domain}/             #   REST API 컨트롤러
│   │   │   │   └── web/{domain}/                #   Thymeleaf MVC 컨트롤러
│   │   │   ├── application/                     # ② 응용 계층
│   │   │   │   └── {domain}/
│   │   │   │       ├── dto/request/             #   요청 DTO
│   │   │   │       ├── dto/response/            #   응답 DTO
│   │   │   │       └── mapper/                  #   MapStruct Entity↔DTO 변환
│   │   │   ├── domain/                          # ③ 도메인 계층
│   │   │   │   └── {domain}/
│   │   │   │       ├── entity/                  #   엔티티 (DB 테이블 매핑)
│   │   │   │       ├── service/                 #   서비스 인터페이스
│   │   │   │       ├── service/impl/            #   서비스 구현체
│   │   │   │       └── repository/mybatis/      #   MyBatis @Mapper 인터페이스
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
│   │       ├── application.yml                  # 공통 설정
│   │       ├── application-local.yml            # 로컬 개발 환경
│   │       ├── application-dev.yml              # 개발 서버
│   │       ├── application-prod.yml             # 운영 서버
│   │       ├── static/
│   │       │   ├── css/, js/                    # 정적 리소스
│   │       │   └── mybatis/mapper/**/*.xml      # MyBatis SQL 매퍼
│   │       └── templates/                       # Thymeleaf HTML 템플릿
│   └── test/
│       └── resources/application-test.yml       # 테스트 환경 (H2)
│
├── deploy/                                      # 배포 관련 설정 모음
│   ├── k8s/                                     #   Kubernetes 배포 매니페스트
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   ├── configmap.yaml                       #   비민감 설정 (DB_HOST 등)
│   │   └── secret.yaml                          #   민감 정보 (DB_PASSWORD 등)
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
├── Jenkinsfile                                  # CI/CD 파이프라인 (루트 필수)
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

### 이중 컨트롤러 패턴

각 도메인은 두 종류의 컨트롤러를 가진다.

| 위치 | 역할 | 반환 타입 |
|---|---|---|
| `interfaces/api/v1/{domain}/` | JSON REST API | `ApiResponse<T>` |
| `interfaces/web/{domain}/` | Thymeleaf 페이지 렌더링 | `String` (뷰 이름) |

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

### 파이프라인 구조

```
① Checkout
② Build & Test         (mvn clean package, JUnit 리포트)
③ Code Quality         (Checkstyle)
④ Security Scan        (OWASP Dependency Check + SpotBugs — 병렬 실행)
⑤ Docker Build         (Docker 기반 방식만 실행)
⑥ Image Security Scan  (Docker 기반 방식만 실행 — Trivy)
⑦ Docker Hub Push      (dockerhub-compose, kubernetes만)
⑧ Deploy               (선택한 DEPLOY_METHOD 스테이지 1개만 실행)
```

### Jenkins에서 실행하는 방법

1. Jenkins 파이프라인 Job 생성 → Pipeline script from SCM 설정
2. **Build with Parameters** 클릭
3. `DEPLOY_METHOD` 드롭다운에서 원하는 배포 방법 선택
4. Build 시작

---

## 8. 배포 방법별 상세 가이드

### 배포 방법 선택 기준

| 상황 | 권장 방법 |
|---|---|
| 로컬 PC에 JAR만 저장하고 싶을 때 | `local-windows-folder` |
| 로컬 PC에서 Docker 컨테이너로 테스트할 때 | `local-docker` |
| 내 회사 서버에 JAR 직접 배포할 때 | `remote-ssh` |
| 내 회사 서버에 Docker로 배포할 때 ★ | `dockerhub-compose` |
| 클라우드/대규모 서비스 배포할 때 ★ | `kubernetes` |
| 고객사에 JAR + 스크립트 패키지로 납품할 때 | `onpremise-zip` |
| 고객사에 Docker 이미지 패키지로 납품할 때 | `onpremise-docker` |

---

### `local-windows-folder` — JAR 파일만 로컬 저장

빌드된 JAR를 이 PC의 지정 폴더에 복사한다. 앱 실행 없이 파일만 저장.

**수정할 설정 없음** — 사전 조건만 확인.

**사전 조건**
```
Jenkins 컨테이너 실행 시 볼륨 마운트 필요:
  -v "C:\SJSJSS\Project\01.File\StarterMavenProject:/var/deploy"
```

**결과물**: `C:\SJSJSS\Project\01.File\StarterMavenProject\app.jar`

---

### `local-windows-docker` — 로컬 JAR 저장 + Docker 컨테이너 실행

JAR를 로컬 폴더에 저장하고 Docker 이미지로도 빌드해 컨테이너를 즉시 실행한다.

**Jenkinsfile 수정 불필요** — Jenkins Credentials만 등록.

**사전 조건**
```
① Docker Desktop 실행 중
② Jenkins 볼륨 마운트 설정 (위와 동일)
③ Jenkins Credentials 등록:
   Jenkins 관리 → Credentials → Global → Add Credentials
     Kind    : Username with password
     ID      : db-credentials
     Username: DB 계정명
     Password: DB 비밀번호
```

**결과물**: `http://localhost:8081` (컨테이너 실행)

---

### `local-docker` — Docker 컨테이너로 로컬 실행

이미지 빌드 후 현재 PC에서 컨테이너를 즉시 실행한다.

**사전 조건**
```
① Docker Desktop 실행 중
② Jenkins Credentials: db-credentials (위와 동일)
```

**결과물**: `http://localhost:8081`

---

### `remote-ssh` — 원격 서버에 JAR 배포

빌드된 JAR를 원격 Linux 서버로 전송해 자동 실행한다.

**Jenkinsfile 상단 수정 필요**

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

**결과물**: 원격 서버 `서버IP:8081`에서 앱 구동

---

### `dockerhub-compose` ★ — Docker Hub + 원격 서버 docker-compose

이미지를 Docker Hub에 Push하고 원격 서버에서 `docker-compose`로 실행한다.  
**중소규모 SaaS 서비스의 현업 표준.**

**Jenkinsfile 수정 필요**

```groovy
// Jenkinsfile — environment 블록
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
   (remote-ssh와 동일)
```

**원격 서버 사전 조건**
```bash
# 원격 서버에 Docker + docker-compose 설치 필요
docker --version        # 20.10+
docker-compose --version  # 또는 docker compose version
```

**결과물**: 원격 서버에서 컨테이너 구동

---

### `kubernetes` ★ — Kubernetes 클러스터 롤링 배포

이미지를 빌드해 레지스트리에 Push하고 K8s 클러스터에 자동 배포한다.  
**대규모/클라우드 SaaS 서비스의 현업 표준.**

**Jenkinsfile 수정 필요**

```groovy
DOCKER_IMAGE   = "실제_도커허브_ID/spring-starter-maven"  // ← 변경
K8S_NAMESPACE  = 'default'                               // ← 네임스페이스 변경 시
```

**k8s 매니페스트 수정 필요**

```yaml
# deploy/k8s/configmap.yaml — 비민감 설정
data:
  SPRING_PROFILES_ACTIVE: "prod"
  DB_HOST: "실제_DB서버_IP"    # ← 변경
  DB_PORT: "3306"
  DB_NAME: "SJSJSS"

# deploy/k8s/secret.yaml — 민감 정보 (Base64 인코딩 또는 stringData 사용)
stringData:
  DB_USERNAME: "실제_계정명"    # ← 변경
  DB_PASSWORD: "실제_비밀번호"  # ← 변경

# deploy/k8s/deployment.yaml
containers:
  - image: IMAGE_PLACEHOLDER   # Jenkins가 자동으로 실제 이미지로 치환
```

**Jenkins Credentials 등록 (2개)**
```
① dockerhub-credentials (위와 동일)

② kubeconfig
   Jenkins 관리 → Credentials → Global → Add Credentials
   Kind : Secret file
   ID   : kubeconfig
   File : ~/.kube/config 파일 업로드
```

**결과물**: K8s 클러스터에서 롤링 업데이트, `LoadBalancer IP:80` 접속

---

### `onpremise-zip` — B2B 고객사 설치형 JAR 패키지

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

### `onpremise-docker` — B2B 고객사 설치형 Docker 패키지

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

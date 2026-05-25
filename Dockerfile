# ============================================================
# Dockerfile — Docker 이미지 빌드 설정
# ============================================================
# 역할:
#   Spring Boot 앱을 실행할 수 있는 Docker 이미지를 만드는 레시피 파일.
#   이 파일을 기반으로 "docker build" 명령이 이미지를 생성한다.
#
# 멀티 스테이지 빌드 전략:
#   Stage 1 (builder): JDK + Maven으로 소스 컴파일 → JAR 생성
#   Stage 2 (runtime): 최종 이미지에는 JRE + JAR 파일만 포함
#
#   이렇게 나누는 이유:
#     - 빌드 도구(Maven, JDK 컴파일러)는 실행 시에는 필요 없음
#     - 최종 이미지에서 제거 → 이미지 크기 최소화 + 보안 공격 표면 축소
#     - JDK 이미지 ~340MB vs JRE 이미지 ~185MB (alpine 기준)
#
# 빌드 명령어:
#   docker build -t spring-app:latest .
#
# 실행 명령어:
#   docker run -d -p 8081:8081 \
#     -e SPRING_PROFILES_ACTIVE=dev \
#     -e DB_HOST=localhost \
#     -e DB_USERNAME=root \
#     -e DB_PASSWORD=secret \
#     spring-app:latest
# ============================================================


# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: Builder — 소스코드를 컴파일해 실행 가능한 JAR 파일 생성
# ─────────────────────────────────────────────────────────────────────────────

# FROM: 베이스 이미지 지정 (이 이미지 위에 레이어를 쌓는다)
#   eclipse-temurin : OpenJDK의 Adoptium 배포판. 기업 환경에서 무료 사용 가능한 표준 배포판
#   17              : Java LTS(Long Term Support) 버전. 2029년까지 보안 지원
#   jdk             : 컴파일러(javac) 포함 버전. 빌드 스테이지에만 필요
#   alpine          : 경량 Linux 배포판(~5MB). 일반 Ubuntu/Debian 기반보다 이미지 크기 대폭 절감
# AS builder        : 이 스테이지에 "builder"라는 이름 부여 (Stage 2에서 참조용)
FROM eclipse-temurin:17-jdk-alpine AS builder

# WORKDIR: 이후 모든 명령어의 작업 디렉터리를 /app 으로 설정
#   없으면 루트(/)에서 작업하게 되어 파일 관리가 어려워짐
WORKDIR /app

# ── 의존성 캐싱 최적화 ──────────────────────────────────────────────────────
# Docker는 각 명령어를 레이어 단위로 캐싱한다.
# 레이어의 내용이 바뀌면 그 레이어와 그 이후 레이어는 캐시 무효화 → 재실행.
#
# 최적화 전략:
#   1. 자주 바뀌는 파일(소스코드)은 나중에 COPY
#   2. 잘 안 바뀌는 파일(pom.xml)을 먼저 COPY → 의존성 다운로드 레이어 캐시 재사용
#
# 결과: 소스만 바뀌면 의존성 재다운로드 없이 빌드 가능 → 빌드 시간 단축
COPY .mvn/  .mvn/   # Maven Wrapper 설정 파일 복사 (.mvn/wrapper/maven-wrapper.properties)
COPY mvnw   ./      # Maven Wrapper 실행 스크립트 복사
RUN  chmod +x mvnw  # 실행 권한 부여 (Git checkout 과정에서 실행 권한이 제거될 수 있음)

COPY pom.xml ./
# dependency:go-offline: pom.xml에 선언된 모든 의존성을 로컬 캐시(.m2)에 미리 다운로드
# -q: quiet 모드 (다운로드 로그 생략). 이후 소스 변경 시 이 레이어는 캐시에서 재사용됨
RUN  ./mvnw dependency:go-offline -q

# 소스 코드 복사 (의존성 다운로드 이후에 복사 → 소스 변경 시 이 레이어부터만 재실행)
COPY src ./src

# 빌드 실행: 테스트 생략(-DskipTests)하고 JAR 파일만 생성
# -q: quiet 모드. 결과물: target/app.jar (pom.xml <finalName>app</finalName>으로 고정)
RUN  ./mvnw clean package -DskipTests -q


# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: Runtime — 실행만을 위한 최소 이미지
# ─────────────────────────────────────────────────────────────────────────────

# jre: Java Runtime Environment. 실행에만 필요한 최소 구성 (컴파일러 제외)
# builder 스테이지의 소스코드, Maven, JDK는 이 이미지에 포함되지 않음
# → 이미지 크기 절감 + 불필요한 도구가 없어 보안 공격 표면 축소
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# ── 보안: 비루트(non-root) 사용자로 실행 ────────────────────────────────────
# 컨테이너가 root로 실행되면 컨테이너 탈출 공격 시 호스트 권한까지 획득 가능.
# 전용 시스템 사용자(appuser)를 만들어 최소 권한으로 앱을 실행.
#   -S: 시스템 계정 (로그인 불가, 홈 디렉터리 없음)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# builder 스테이지(Stage 1)의 파일 시스템에서 JAR 파일만 복사
# --from=builder: Stage 1에서 생성된 파일 참조
# /app/target/*.jar → /app/app.jar 로 이름 변경
COPY --from=builder /app/target/*.jar app.jar

# 이후 명령어(ENTRYPOINT 포함)는 appuser 권한으로 실행
USER appuser

# EXPOSE: 컨테이너가 사용할 포트를 문서화 (실제 포트 열기는 "docker run -p 8081:8081"에서 함)
EXPOSE 8081

# ── 헬스체크 ─────────────────────────────────────────────────────────────────
# Docker/docker-compose가 컨테이너 상태를 주기적으로 확인하는 방법.
# Kubernetes는 deployment.yaml의 readinessProbe/livenessProbe를 따로 사용.
#
# --interval=30s    : 30초마다 한 번씩 체크
# --timeout=10s     : 명령이 10초 내 완료되지 않으면 실패로 처리
# --start-period=60s: 앱 기동 중(최초 60초)에는 실패를 카운트하지 않음 (기동 시간 여유 부여)
# --retries=3       : 3번 연속 실패해야 "unhealthy"로 표시 (일시적 오류에 관대하게)
#
# wget --spider: 파일 다운로드 없이 URL 접근 가능 여부만 확인
# (alpine 이미지에는 curl이 없고 wget이 기본 내장됨)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8081/actuator/health || exit 1

# ── 애플리케이션 실행 ────────────────────────────────────────────────────────
# ENTRYPOINT: 컨테이너 시작 시 실행할 명령어
#
# ["sh", "-c", "..."] 형태로 셸을 통해 실행하는 이유:
#   $JAVA_OPTS 같은 환경변수를 셸이 확장(expand)해주어야 하기 때문.
#   ["java", "$JAVA_OPTS", "-jar", "app.jar"] 방식은 $JAVA_OPTS가 문자 그대로 전달됨.
#
# JAVA_OPTS: 외부에서 JVM 옵션을 주입할 수 있는 환경변수
#   예: -e JAVA_OPTS="-Xms256m -Xmx512m" (docker run 시 메모리 제한)
#
# -Djava.security.egd=file:/dev/./urandom:
#   SecureRandom 초기화 속도 개선. 기본값(/dev/random)은 엔트로피 부족 시
#   Tomcat 시작이 수십 초~수분 지연될 수 있음. urandom은 블로킹 없이 빠름.
ENTRYPOINT ["sh", "-c", \
  "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

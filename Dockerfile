# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 : Build
#   - pom.xml만 먼저 복사해 의존성 레이어를 캐싱 → 소스 변경 시 재다운로드 없음
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY .mvn/     .mvn/
COPY mvnw      ./
RUN  chmod +x  mvnw

# 의존성 캐싱 레이어 (pom.xml이 바뀌지 않으면 이 레이어는 재사용)
COPY pom.xml   ./
RUN  ./mvnw dependency:go-offline -q

# 소스 빌드
COPY src ./src
RUN  ./mvnw clean package -DskipTests -q


# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 : Runtime  (JRE만 포함 → JDK 제외로 이미지 경량화)
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 보안: 비루트 사용자로 실행
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar

USER appuser

EXPOSE 8081

# -Djava.security.egd : Tomcat 시작 속도 개선
# JAVA_OPTS           : 외부에서 JVM 튜닝 옵션 주입 가능 (예: -Xms256m -Xmx512m)
ENTRYPOINT ["sh", "-c", \
  "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

#!/bin/bash
# ============================================================
# start.sh — 애플리케이션 시작 스크립트 (Linux / macOS)
# ============================================================
# 사용법:
#   chmod +x bin/start.sh   (최초 1회, 실행 권한 부여)
#   bin/start.sh
#
# 실행 방법:
#   설치 루트(app.jar가 있는 폴더)로 이동 후 실행하거나,
#   스크립트 경로를 직접 지정해 실행.
#   예: cd /opt/spring-starter-maven && ./bin/start.sh
#
# 로그 확인:
#   tail -f logs/application.log
#   tail -f logs/start.log
# ============================================================

# ── 경로 계산 ─────────────────────────────────────────────────────────────────
# 스크립트 위치 기준으로 앱 루트 디렉터리를 찾음
# bin/ 하위에 스크립트가 있으므로 부모 디렉터리가 앱 루트
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="$(dirname "$SCRIPT_DIR")"
APP_JAR="$APP_DIR/app.jar"
CONFIG_DIR="$APP_DIR/config"
LOG_DIR="$APP_DIR/logs"
PID_FILE="$APP_DIR/app.pid"
START_LOG="$LOG_DIR/start.log"

# ── 사전 검사 ─────────────────────────────────────────────────────────────────
if [ ! -f "$APP_JAR" ]; then
    echo "❌ app.jar 파일을 찾을 수 없습니다: $APP_JAR"
    echo "   설치 루트 디렉터리에서 실행하고 있는지 확인하세요."
    exit 1
fi

if ! command -v java &>/dev/null; then
    echo "❌ Java가 설치되지 않았거나 PATH에 없습니다."
    echo "   JDK 17 이상을 설치하고 PATH에 java를 추가하세요."
    echo "   확인: which java | java -version"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ -n "$JAVA_VERSION" ] && [ "$JAVA_VERSION" -lt 17 ] 2>/dev/null; then
    echo "⚠ Java 버전 경고: JDK 17 이상이 필요합니다. (현재: $JAVA_VERSION)"
    echo "  계속 진행하시겠습니까? (y/N)"
    read -r ANSWER
    if [[ "$ANSWER" != [Yy] ]]; then
        exit 1
    fi
fi

# ── 중복 실행 방지 ────────────────────────────────────────────────────────────
if [ -f "$PID_FILE" ]; then
    EXISTING_PID=$(cat "$PID_FILE")
    if kill -0 "$EXISTING_PID" 2>/dev/null; then
        echo "⚠ 애플리케이션이 이미 실행 중입니다. (PID: $EXISTING_PID)"
        echo "  재시작하려면 먼저 bin/stop.sh를 실행하세요."
        exit 1
    else
        echo "  이전 PID 파일이 남아있지만 프로세스가 없어 정리합니다."
        rm -f "$PID_FILE"
    fi
fi

# ── 디렉터리 생성 ─────────────────────────────────────────────────────────────
mkdir -p "$LOG_DIR"

# ── JVM 옵션 설정 ─────────────────────────────────────────────────────────────
# 메모리 설정: 서버 메모리에 맞게 조정
# Xms: 초기 힙 메모리 (작게 시작)
# Xmx: 최대 힙 메모리 (서버 전체 메모리의 50~70% 권장)
# 예: 서버 RAM 4GB → Xmx2g, 서버 RAM 8GB → Xmx4g
JVM_OPTS="-Xms256m -Xmx1g"

# GC 로그 (성능 문제 진단 시 활성화, 평상시 주석 처리 권장)
# JVM_OPTS="$JVM_OPTS -XX:+PrintGCDetails -Xlog:gc*:file=$LOG_DIR/gc.log"

# ── 애플리케이션 시작 ─────────────────────────────────────────────────────────
echo "=========================================="
echo "  Spring Starter Maven 앱 시작"
echo "=========================================="
echo "  앱 루트  : $APP_DIR"
echo "  설정 파일: $CONFIG_DIR/application.yml"
echo "  로그 경로: $LOG_DIR/application.log"
echo "  Java     : $(java -version 2>&1 | head -1)"
echo "=========================================="
echo ""

# nohup: 터미널 종료 후에도 백그라운드 실행 유지
# app.jar 옆에 config/ 디렉터리가 있으므로 Spring Boot가 자동으로 외부 설정 로드
nohup java $JVM_OPTS \
    -jar "$APP_JAR" \
    >> "$START_LOG" 2>&1 &

APP_PID=$!
echo $APP_PID > "$PID_FILE"

echo "  시작 요청 완료 (PID: $APP_PID)"
echo "  약 30~60초 후 서비스가 준비됩니다."
echo ""
echo "  ── 상태 확인 명령어 ────────────────────────────────"
echo "  로그 확인  : tail -f $LOG_DIR/application.log"
echo "  헬스체크   : curl http://localhost:8080/actuator/health"
echo "  프로세스   : ps -p $APP_PID"
echo "  ─────────────────────────────────────────────────"

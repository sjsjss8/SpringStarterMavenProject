#!/bin/bash
# ============================================================
# stop.sh — 애플리케이션 종료 스크립트 (Linux / macOS)
# ============================================================
# 사용법:
#   bin/stop.sh              정상 종료 (SIGTERM, 30초 대기)
#   bin/stop.sh --force      강제 종료 (SIGKILL, 즉시 종료)
#
# 종료 방식:
#   1. SIGTERM 전송 → Spring Boot graceful shutdown 시작
#      (진행 중인 요청 처리 완료 후 종료, 최대 30초)
#   2. 30초 후에도 종료 안 되면 SIGKILL로 강제 종료
#   3. --force 옵션: SIGKILL 즉시 전송 (데이터 유실 위험)
# ============================================================

# ── 경로 계산 ─────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="$(dirname "$SCRIPT_DIR")"
PID_FILE="$APP_DIR/app.pid"
FORCE_STOP=false

# ── 옵션 파싱 ─────────────────────────────────────────────────────────────────
if [[ "$1" == "--force" ]]; then
    FORCE_STOP=true
fi

# ── PID 파일 확인 ─────────────────────────────────────────────────────────────
if [ ! -f "$PID_FILE" ]; then
    echo "⚠ PID 파일이 없습니다: $PID_FILE"
    echo "  앱이 실행 중이지 않거나, 수동으로 시작된 경우입니다."
    echo ""
    echo "  실행 중인 앱 프로세스를 직접 찾으려면:"
    echo "  ps aux | grep app.jar"
    exit 1
fi

APP_PID=$(cat "$PID_FILE")

# ── 프로세스 존재 확인 ────────────────────────────────────────────────────────
if ! kill -0 "$APP_PID" 2>/dev/null; then
    echo "⚠ PID $APP_PID 프로세스가 존재하지 않습니다."
    echo "  이미 종료된 상태입니다. PID 파일을 정리합니다."
    rm -f "$PID_FILE"
    exit 0
fi

# ── 강제 종료 ─────────────────────────────────────────────────────────────────
if [ "$FORCE_STOP" = true ]; then
    echo "⚠ 강제 종료 (SIGKILL) — PID: $APP_PID"
    kill -9 "$APP_PID"
    rm -f "$PID_FILE"
    echo "✅ 강제 종료 완료"
    exit 0
fi

# ── 정상 종료 (Graceful Shutdown) ─────────────────────────────────────────────
echo "=========================================="
echo "  Spring Starter Maven 앱 종료"
echo "=========================================="
echo "  PID: $APP_PID"
echo "  방식: Graceful Shutdown (최대 30초 대기)"
echo "=========================================="

# SIGTERM 전송: Spring Boot가 진행 중인 요청을 완료하고 정상 종료
kill -TERM "$APP_PID"
echo ""
echo "  SIGTERM 전송 완료. 종료 대기 중..."

# 최대 30초 대기 (1초 간격으로 확인)
WAIT_SECONDS=30
COUNT=0
while kill -0 "$APP_PID" 2>/dev/null; do
    sleep 1
    COUNT=$((COUNT + 1))
    if [ $((COUNT % 5)) -eq 0 ]; then
        echo "  대기 중... ${COUNT}/${WAIT_SECONDS}초"
    fi
    if [ "$COUNT" -ge "$WAIT_SECONDS" ]; then
        echo ""
        echo "  ⚠ ${WAIT_SECONDS}초 초과 — 강제 종료합니다 (SIGKILL)"
        kill -9 "$APP_PID" 2>/dev/null
        break
    fi
done

rm -f "$PID_FILE"
echo ""
echo "✅ 앱 종료 완료 (PID: $APP_PID)"

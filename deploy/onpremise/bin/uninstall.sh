#!/bin/bash
# ============================================================
# uninstall.sh — 앱 제거 스크립트 (Linux / macOS)
# ============================================================
# 역할:
#   실행 중인 앱을 종료하고, systemd 서비스를 해제한 뒤
#   PID 파일·로그·백업 파일을 선택적으로 정리한다.
#
# 주의:
#   app.jar, config/, mapper/ 파일은 삭제하지 않는다. (데이터 보호)
#   완전 삭제는 이 스크립트 실행 후 폴더 전체를 수동으로 삭제한다.
#
# 사용법:
#   bin/uninstall.sh
#   sudo bin/uninstall.sh   (systemd 서비스 해제 포함)
# ============================================================

set -e

# ── 색상 출력 헬퍼 ────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()    { echo -e "${CYAN}[INFO]${NC}  $1"; }
success() { echo -e "${GREEN}[OK]${NC}    $1"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $1"; }
error()   { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# ── 경로 계산 ─────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="$(dirname "$SCRIPT_DIR")"
PID_FILE="$APP_DIR/app.pid"
CONFIG_FILE="$APP_DIR/config/application.yml"

echo ""
echo "=========================================="
echo "  Spring Starter Maven — 제거 시작"
echo "=========================================="
echo ""
warn "이 스크립트는 다음을 수행합니다:"
echo "   ✓ 실행 중인 앱 종료"
echo "   ✓ systemd 서비스 해제 (등록된 경우, root 권한 필요)"
echo "   ✓ PID 파일·로그·백업 파일 정리 (선택)"
echo "   ✗ app.jar, config/, mapper/ 는 삭제하지 않음"
echo ""
read -rp "  제거를 진행하시겠습니까? (y/N) : " CONFIRM
[[ "$CONFIRM" =~ ^[Yy]$ ]] || { echo "  제거를 취소합니다."; exit 0; }
echo ""

# ── 앱 종료 ───────────────────────────────────────────────────────────────────
info "실행 중인 앱 확인 중..."

if [ -f "$PID_FILE" ]; then
    APP_PID=$(cat "$PID_FILE")
    if kill -0 "$APP_PID" 2>/dev/null; then
        info "앱 종료 중 (PID: $APP_PID)..."
        "$APP_DIR/bin/stop.sh"
    else
        info "앱이 이미 종료되어 있습니다. (PID 파일만 남아 있음)"
        rm -f "$PID_FILE"
    fi
else
    # PID 파일 없을 때 프로세스 직접 탐색
    APP_PID=$(pgrep -f "app\.jar" 2>/dev/null || true)
    if [ -n "$APP_PID" ]; then
        info "앱 종료 중 (PID: $APP_PID)..."
        kill -TERM "$APP_PID" 2>/dev/null || true
        sleep 3
        if kill -0 "$APP_PID" 2>/dev/null; then
            warn "SIGTERM 후에도 프로세스 살아있음 — SIGKILL 전송"
            kill -KILL "$APP_PID" 2>/dev/null || true
        fi
        success "앱 종료 완료"
    else
        info "실행 중인 앱 프로세스 없음"
    fi
fi

# ── systemd 서비스 해제 ───────────────────────────────────────────────────────
if command -v systemctl &>/dev/null; then
    if [ "$(id -u)" -eq 0 ]; then
        if systemctl list-unit-files spring-app.service &>/dev/null 2>&1 \
            && systemctl is-enabled spring-app &>/dev/null 2>&1; then
            info "systemd 서비스 해제 중..."
            systemctl stop    spring-app 2>/dev/null || true
            systemctl disable spring-app 2>/dev/null || true
            rm -f /etc/systemd/system/spring-app.service
            systemctl daemon-reload
            success "systemd 서비스 (spring-app) 해제 완료"
        else
            info "등록된 systemd 서비스 없음 (spring-app)"
        fi
    else
        # root 아닌 경우 서비스 등록 여부만 확인해서 안내
        if systemctl list-unit-files spring-app.service &>/dev/null 2>&1; then
            warn "systemd 서비스 해제는 root 권한이 필요합니다."
            warn "  sudo systemctl stop spring-app"
            warn "  sudo systemctl disable spring-app"
            warn "  sudo rm /etc/systemd/system/spring-app.service"
            warn "  sudo systemctl daemon-reload"
        fi
    fi
fi
echo ""

# ── 파일 정리 (선택) ──────────────────────────────────────────────────────────
echo "── 추가 파일 정리 (선택) ──────────────────────────────────────"

# 로그 디렉터리
if [ -d "$APP_DIR/logs" ] && [ "$(ls -A "$APP_DIR/logs" 2>/dev/null)" ]; then
    read -rp "  로그 파일을 삭제하시겠습니까? (logs/) (y/N) : " DEL_LOGS
    if [[ "$DEL_LOGS" =~ ^[Yy]$ ]]; then
        rm -rf "$APP_DIR/logs"
        success "로그 디렉터리 삭제 완료"
    else
        info "로그 유지: $APP_DIR/logs"
    fi
fi

# config 백업 파일
if [ -f "${CONFIG_FILE}.bak" ]; then
    read -rp "  설정 백업 파일을 삭제하시겠습니까? (config/application.yml.bak) (y/N) : " DEL_BAK
    if [[ "$DEL_BAK" =~ ^[Yy]$ ]]; then
        rm -f "${CONFIG_FILE}.bak"
        success "설정 백업 파일 삭제 완료"
    fi
fi

# PID 파일 정리
rm -f "$PID_FILE"

# ── 완료 요약 ────────────────────────────────────────────────────────────────
echo ""
echo "=========================================="
success "제거 완료!"
echo "=========================================="
echo ""
echo "  보존된 파일:"
echo "    app.jar, config/, mapper/   ← 데이터 보호를 위해 유지"
echo ""
echo "  완전 삭제를 원하면 이 폴더 전체를 삭제하세요:"
echo "    rm -rf \"$APP_DIR\""
echo ""

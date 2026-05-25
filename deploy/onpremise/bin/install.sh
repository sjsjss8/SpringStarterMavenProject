#!/bin/bash
# ============================================================
# install.sh — 최초 설치 스크립트 (Linux / macOS)
# ============================================================
# 역할:
#   ZIP 압축 해제 후 최초 1회 실행하는 자동 설치 스크립트.
#   대화형으로 DB 접속 정보를 입력받아 config/application.yml에
#   자동으로 반영하고, 선택적으로 systemd 서비스를 등록한 뒤 앱을 시작한다.
#
# 사용법:
#   chmod +x bin/install.sh   (최초 1회 실행 권한 부여)
#   bin/install.sh
#
# 주의:
#   이미 설치된 경우 재실행하면 기존 설정을 덮어씌움.
#   설정만 변경하려면 config/application.yml 을 직접 편집 후 bin/stop.sh → bin/start.sh
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
CONFIG_FILE="$APP_DIR/config/application.yml"
APP_JAR="$APP_DIR/app.jar"

echo ""
echo "=========================================="
echo "  Spring Starter Maven — 설치 시작"
echo "=========================================="
echo ""

# ── 사전 검사 ─────────────────────────────────────────────────────────────────
info "환경 확인 중..."

[ -f "$APP_JAR" ]      || error "app.jar 를 찾을 수 없습니다: $APP_JAR"
[ -f "$CONFIG_FILE" ]  || error "config/application.yml 을 찾을 수 없습니다: $CONFIG_FILE"

command -v java &>/dev/null || error "Java가 설치되지 않았거나 PATH에 없습니다. JDK 17 이상을 설치하세요."

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
[ "${JAVA_VER:-0}" -ge 17 ] 2>/dev/null || warn "Java 17 이상을 권장합니다. (감지된 버전: ${JAVA_VER:-알 수 없음})"

command -v sed  &>/dev/null || error "sed 명령어가 없습니다."

success "환경 확인 완료 (Java ${JAVA_VER})"
echo ""

# ── DB 접속 정보 입력 ─────────────────────────────────────────────────────────
echo "── 데이터베이스 접속 정보를 입력하세요 ──────────────────────"
echo "   (기본값이 있는 항목은 Enter를 누르면 기본값이 사용됩니다)"
echo ""

read -rp "  DB 호스트       [localhost] : " DB_HOST
DB_HOST="${DB_HOST:-localhost}"

read -rp "  DB 포트         [3306]      : " DB_PORT
DB_PORT="${DB_PORT:-3306}"

read -rp "  DB 이름         [SJSJSS]    : " DB_NAME
DB_NAME="${DB_NAME:-SJSJSS}"

read -rp "  DB 계정         : " DB_USERNAME
[ -n "$DB_USERNAME" ] || error "DB 계정은 필수 입력값입니다."

read -rsp "  DB 비밀번호     : " DB_PASSWORD
echo ""
[ -n "$DB_PASSWORD" ] || error "DB 비밀번호는 필수 입력값입니다."

echo ""
read -rp "  앱 포트         [8080]      : " APP_PORT
APP_PORT="${APP_PORT:-8080}"

echo ""

# ── 입력값 확인 ───────────────────────────────────────────────────────────────
echo "── 입력 내용 확인 ──────────────────────────────────────────"
echo "   DB URL  : jdbc:mariadb://${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "   DB 계정 : ${DB_USERNAME}"
echo "   앱 포트 : ${APP_PORT}"
echo ""
read -rp "  위 내용으로 설치를 진행하시겠습니까? (y/N) : " CONFIRM
[[ "$CONFIRM" =~ ^[Yy]$ ]] || { echo "  설치를 취소합니다."; exit 0; }
echo ""

# ── config/application.yml 업데이트 ──────────────────────────────────────────
info "config/application.yml 설정 중..."

# 백업 생성 (재설치 시에도 원본 보존)
cp "$CONFIG_FILE" "${CONFIG_FILE}.bak"

# DB URL 교체 (jdbc:mariadb://localhost:3306/SJSJSS 형태를 실제 값으로)
sed -i "s|url: jdbc:mariadb://[^[:space:]]*|url: jdbc:mariadb://${DB_HOST}:${DB_PORT}/${DB_NAME}|" "$CONFIG_FILE"

# DB 계정/비밀번호 교체 (your_db_user, your_db_password 플레이스홀더 교체)
sed -i "s|username: your_db_user|username: ${DB_USERNAME}|" "$CONFIG_FILE"
sed -i "s|password: your_db_password|password: ${DB_PASSWORD}|" "$CONFIG_FILE"

# 앱 포트 교체 (server.port 아래의 port 값)
sed -i "/^server:/,/^[^ ]/ s|  port: [0-9]*|  port: ${APP_PORT}|" "$CONFIG_FILE"

success "config/application.yml 설정 완료"
echo "   백업 파일: ${CONFIG_FILE}.bak"
echo ""

# ── 로그 디렉터리 생성 ────────────────────────────────────────────────────────
mkdir -p "$APP_DIR/logs"

# ── systemd 서비스 등록 (선택) ────────────────────────────────────────────────
SYSTEMD_INSTALLED=false

if command -v systemctl &>/dev/null && [ "$(id -u)" -eq 0 ]; then
    echo "── systemd 서비스 등록 ──────────────────────────────────────"
    echo "   서비스로 등록하면 서버 재시작 시 앱이 자동으로 시작됩니다."
    read -rp "  systemd 서비스로 등록하시겠습니까? (y/N) : " REG_SERVICE

    if [[ "$REG_SERVICE" =~ ^[Yy]$ ]]; then
        SERVICE_FILE="/etc/systemd/system/spring-app.service"
        info "systemd 서비스 파일 생성 중: $SERVICE_FILE"

        cat > "$SERVICE_FILE" <<EOF
[Unit]
Description=Spring Starter Maven Application
After=network.target

[Service]
Type=simple
User=$(whoami)
WorkingDirectory=${APP_DIR}
ExecStart=${APP_DIR}/bin/start.sh
ExecStop=${APP_DIR}/bin/stop.sh
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

        systemctl daemon-reload
        systemctl enable spring-app
        SYSTEMD_INSTALLED=true
        success "systemd 서비스 등록 완료 (spring-app)"
    fi
    echo ""
elif command -v systemctl &>/dev/null && [ "$(id -u)" -ne 0 ]; then
    warn "systemd 서비스 등록은 root 권한이 필요합니다. (현재: 일반 사용자)"
    warn "서비스 등록을 원하면 sudo bin/install.sh 로 재실행하세요."
    echo ""
fi

# ── 설정 파일 권한 보호 ───────────────────────────────────────────────────────
chmod 600 "$CONFIG_FILE"
info "config/application.yml 권한 설정 완료 (600 — 소유자만 읽기/쓰기)"
echo ""

# ── 앱 시작 ───────────────────────────────────────────────────────────────────
read -rp "지금 바로 앱을 시작하시겠습니까? (Y/n) : " START_NOW
START_NOW="${START_NOW:-Y}"

if [[ "$START_NOW" =~ ^[Yy]$ ]]; then
    "$APP_DIR/bin/start.sh"
fi

# ── 설치 완료 요약 ────────────────────────────────────────────────────────────
echo ""
echo "=========================================="
success "설치 완료!"
echo "=========================================="
echo ""
echo "  앱 URL       : http://$(hostname -I | awk '{print $1}' 2>/dev/null || echo 'localhost'):${APP_PORT}"
echo "  로그 경로    : ${APP_DIR}/logs/application.log"
echo "  설정 파일    : ${CONFIG_FILE}"
echo ""
echo "  ── 이후 관리 명령어 (Linux) ──────────────────────────"
if [ "$SYSTEMD_INSTALLED" = true ]; then
echo "  시작  : sudo systemctl start  spring-app"
echo "  종료  : sudo systemctl stop   spring-app"
echo "  재시작: sudo systemctl restart spring-app"
echo "  상태  : sudo systemctl status spring-app"
else
echo "  시작  : bin/start.sh"
echo "  종료  : bin/stop.sh"
fi
echo "  (Windows 사용자: bin\\windows\\start.bat / stop.bat)"
echo "  ─────────────────────────────────────────────────────"
echo ""

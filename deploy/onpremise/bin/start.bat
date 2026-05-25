@echo off
rem ============================================================
rem start.bat - 애플리케이션 시작 스크립트 (Windows)
rem ============================================================
rem 사용법:
rem   bin\start.bat            (탐색기에서 더블클릭 또는 CMD에서 실행)
rem
rem 실행 방법:
rem   설치 루트(app.jar가 있는 폴더)로 이동 후 실행하거나,
rem   탐색기에서 bin\start.bat 파일을 더블클릭.
rem   예: cd C:\app\spring-starter-maven && bin\start.bat
rem
rem 로그 확인:
rem   logs\application.log 파일을 메모장이나 에디터로 열기
rem   또는 PowerShell에서: Get-Content logs\application.log -Wait
rem ============================================================

chcp 65001 > nul
setlocal

rem ── 경로 계산 ─────────────────────────────────────────────────────────────
rem 스크립트 위치(%~dp0)에서 부모 디렉터리(앱 루트)를 계산
set SCRIPT_DIR=%~dp0
rem SCRIPT_DIR 끝에 \가 있으므로 bin\ 을 제거해 앱 루트 경로 계산
set APP_DIR=%SCRIPT_DIR:~0,-1%
for %%I in ("%APP_DIR%") do set APP_DIR=%%~dpI
rem 끝의 \를 제거
if "%APP_DIR:~-1%"=="\" set APP_DIR=%APP_DIR:~0,-1%

set APP_JAR=%APP_DIR%\app.jar
set CONFIG_DIR=%APP_DIR%\config
set LOG_DIR=%APP_DIR%\logs
set PID_FILE=%APP_DIR%\app.pid
set START_LOG=%LOG_DIR%\start.log

rem ── 사전 검사 ─────────────────────────────────────────────────────────────
if not exist "%APP_JAR%" (
    echo [오류] app.jar 파일을 찾을 수 없습니다: %APP_JAR%
    echo        설치 루트 디렉터리에서 실행하고 있는지 확인하세요.
    pause
    exit /b 1
)

java -version > nul 2>&1
if errorlevel 1 (
    echo [오류] Java가 설치되지 않았거나 PATH에 없습니다.
    echo        JDK 17 이상을 설치하고 환경변수 PATH에 java.exe 경로를 추가하세요.
    echo        다운로드: https://adoptium.net/
    pause
    exit /b 1
)

rem ── 중복 실행 방지 ────────────────────────────────────────────────────────
if exist "%PID_FILE%" (
    set /p EXISTING_PID=<"%PID_FILE%"
    rem 해당 PID 프로세스가 실행 중인지 확인
    tasklist /FI "PID eq !EXISTING_PID!" 2>nul | find /I "java" > nul
    if not errorlevel 1 (
        echo [경고] 애플리케이션이 이미 실행 중입니다. PID: !EXISTING_PID!
        echo        재시작하려면 먼저 bin\stop.bat 를 실행하세요.
        pause
        exit /b 1
    ) else (
        echo        이전 PID 파일이 남아있지만 프로세스가 없어 정리합니다.
        del "%PID_FILE%"
    )
)

rem ── 디렉터리 생성 ─────────────────────────────────────────────────────────
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

rem ── JVM 옵션 설정 ─────────────────────────────────────────────────────────
rem 메모리 설정: 서버 메모리에 맞게 조정
rem Xms: 초기 힙 메모리 / Xmx: 최대 힙 메모리 (서버 RAM의 50~70% 권장)
set JVM_OPTS=-Xms256m -Xmx1g

rem ── 애플리케이션 시작 (백그라운드) ──────────────────────────────────────
echo ==========================================
echo   Spring Starter Maven 앱 시작
echo ==========================================
echo   앱 루트  : %APP_DIR%
echo   설정 파일: %CONFIG_DIR%\application.yml
echo   로그 경로: %LOG_DIR%\application.log
echo   Java     :
java -version 2>&1 | findstr /i "version"
echo ==========================================
echo.

rem start /B: 새 윈도우 없이 백그라운드에서 실행
rem config\ 폴더가 app.jar 옆에 있으면 Spring Boot가 자동으로 외부 설정 로드
start /B "" java %JVM_OPTS% -jar "%APP_JAR%" >> "%START_LOG%" 2>&1

rem Windows에서는 PID를 간단히 가져오기 어렵기 때문에 wmic로 마지막 java 프로세스 PID를 저장
timeout /t 2 /nobreak > nul
for /f "tokens=1" %%P in ('wmic process where "name='java.exe'" get processid 2^>nul ^| findstr /r "[0-9]" ^| sort /r') do (
    echo %%P > "%PID_FILE%"
    goto :pid_saved
)
:pid_saved

echo   시작 요청 완료
echo   약 30~60초 후 서비스가 준비됩니다.
echo.
echo   ── 상태 확인 방법 ──────────────────────────────────────
echo   로그 확인  : 로그 파일을 열거나 logs\application.log 를 메모장으로 확인
echo   헬스체크   : 브라우저에서 http://localhost:8080/actuator/health 접속
echo   프로세스   : 작업 관리자(Ctrl+Shift+Esc) 에서 java.exe 확인
echo   ─────────────────────────────────────────────────────
echo.
pause

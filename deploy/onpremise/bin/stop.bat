@echo off
rem ============================================================
rem stop.bat - 애플리케이션 종료 스크립트 (Windows)
rem ============================================================
rem 사용법:
rem   bin\stop.bat             (탐색기에서 더블클릭 또는 CMD에서 실행)
rem
rem 종료 방식:
rem   app.pid 파일에 저장된 PID로 해당 java.exe 프로세스를 종료.
rem   Windows는 SIGTERM을 지원하지 않으므로 taskkill 사용.
rem   진행 중인 요청이 완료될 때까지 기다리지 않으므로,
rem   트래픽이 없는 시간대에 종료 권장.
rem ============================================================

chcp 65001 > nul
setlocal enabledelayedexpansion

rem ── 경로 계산 ─────────────────────────────────────────────────────────────
set SCRIPT_DIR=%~dp0
set APP_DIR=%SCRIPT_DIR:~0,-1%
for %%I in ("%APP_DIR%") do set APP_DIR=%%~dpI
if "%APP_DIR:~-1%"=="\" set APP_DIR=%APP_DIR:~0,-1%

set PID_FILE=%APP_DIR%\app.pid

echo ==========================================
echo   Spring Starter Maven 앱 종료
echo ==========================================

rem ── PID 파일 확인 ─────────────────────────────────────────────────────────
if not exist "%PID_FILE%" (
    echo [경고] PID 파일이 없습니다: %PID_FILE%
    echo        앱이 실행 중이지 않거나 start.bat으로 시작하지 않은 경우입니다.
    echo.
    echo        실행 중인 java.exe 프로세스 목록:
    tasklist /FI "IMAGENAME eq java.exe" 2>nul
    echo.
    echo        수동 종료 방법:
    echo        taskkill /IM java.exe /F
    pause
    exit /b 1
)

set /p APP_PID=<"%PID_FILE%"

rem ── 프로세스 존재 확인 ────────────────────────────────────────────────────
tasklist /FI "PID eq %APP_PID%" 2>nul | find /I "java" > nul
if errorlevel 1 (
    echo [경고] PID %APP_PID% 프로세스가 존재하지 않습니다.
    echo        이미 종료된 상태입니다. PID 파일을 정리합니다.
    del "%PID_FILE%"
    pause
    exit /b 0
)

rem ── 프로세스 종료 ─────────────────────────────────────────────────────────
echo   PID: %APP_PID%
echo   종료 신호 전송 중...

rem /F: 강제 종료 / /PID: 특정 PID만 종료 (같은 서버의 다른 java 프로세스는 영향 없음)
taskkill /PID %APP_PID% /F > nul 2>&1
if errorlevel 1 (
    echo [오류] 프로세스 종료에 실패했습니다.
    echo        관리자 권한으로 실행하거나 작업 관리자에서 직접 종료하세요.
    pause
    exit /b 1
)

del "%PID_FILE%"
echo.
echo [완료] 앱 종료 완료 (PID: %APP_PID%)
echo.
pause

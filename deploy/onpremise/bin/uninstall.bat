@echo off
:: ============================================================
:: uninstall.bat — 앱 제거 스크립트 (Windows)
:: ============================================================
:: 역할:
::   실행 중인 앱을 종료하고, PID 파일·로그·백업 파일을
::   선택적으로 정리한다.
::
:: 주의:
::   app.jar, config\, mapper\ 파일은 삭제하지 않는다. (데이터 보호)
::   완전 삭제는 이 스크립트 실행 후 폴더 전체를 수동으로 삭제한다.
::
:: 사용법:
::   bin\uninstall.bat
:: ============================================================
setlocal EnableDelayedExpansion
chcp 65001 >nul 2>&1

:: ── 경로 계산 ────────────────────────────────────────────────────────────────
pushd "%~dp0.."
set "APP_DIR=%CD%"
popd
set "PID_FILE=%APP_DIR%\app.pid"
set "CONFIG_FILE=%APP_DIR%\config\application.yml"

echo.
echo ==========================================
echo   Spring Starter Maven -- 제거 시작
echo ==========================================
echo.
echo [WARN]  이 스크립트는 다음을 수행합니다:
echo   O 실행 중인 앱 종료
echo   O PID 파일, 로그, 백업 파일 정리 (선택)
echo   X app.jar, config\, mapper\ 는 삭제하지 않음
echo.
set /p "_CONFIRM=  제거를 진행하시겠습니까? (y/N) : "
if /i not "!_CONFIRM!"=="y" (
    echo   제거를 취소합니다.
    pause & exit /b 0
)
echo.

:: ── 앱 종료 ─────────────────────────────────────────────────────────────────
echo [INFO]  실행 중인 앱 확인 중...

set "APP_PID="

:: PID 파일에서 읽기
if exist "%PID_FILE%" (
    set /p "APP_PID=" < "%PID_FILE%"
)

:: PID 파일 없으면 프로세스 직접 탐색
if "!APP_PID!"=="" (
    for /f "skip=1 tokens=2 delims= " %%p in (
        'wmic process where "commandline like \"%%app.jar%%\"" get processid 2^>nul'
    ) do (
        if not "%%p"=="" set "APP_PID=%%p"
    )
)

if not "!APP_PID!"=="" (
    echo [INFO]  앱 종료 중 (PID: !APP_PID!)...
    taskkill /PID !APP_PID! /F >nul 2>&1
    if errorlevel 1 (
        echo [WARN]  프로세스를 찾을 수 없습니다. 이미 종료된 것 같습니다.
    ) else (
        echo [OK]    앱 종료 완료
    )
) else (
    echo [INFO]  실행 중인 앱 프로세스 없음
)

:: PID 파일 삭제
if exist "%PID_FILE%" del /f "%PID_FILE%"

echo.

:: ── 파일 정리 (선택) ─────────────────────────────────────────────────────────
echo -- 추가 파일 정리 (선택) ----------------------------------------

:: 로그 디렉터리
if exist "%APP_DIR%\logs\" (
    :: 로그 폴더 안에 파일이 있는지 확인
    dir /b /a "%APP_DIR%\logs\" 2>nul | findstr . >nul
    if not errorlevel 1 (
        set /p "_DEL_LOGS=  로그 파일을 삭제하시겠습니까? (logs\) (y/N) : "
        if /i "!_DEL_LOGS!"=="y" (
            rmdir /s /q "%APP_DIR%\logs" 2>nul
            echo [OK]    로그 디렉터리 삭제 완료
        ) else (
            echo [INFO]  로그 유지: %APP_DIR%\logs
        )
    ) else (
        echo [INFO]  로그 폴더가 비어 있습니다.
    )
)

:: config 백업 파일
if exist "%CONFIG_FILE%.bak" (
    set /p "_DEL_BAK=  설정 백업 파일을 삭제하시겠습니까? (config\application.yml.bak) (y/N) : "
    if /i "!_DEL_BAK!"=="y" (
        del /f "%CONFIG_FILE%.bak"
        echo [OK]    설정 백업 파일 삭제 완료
    )
)

:: ── 완료 요약 ────────────────────────────────────────────────────────────────
echo.
echo ==========================================
echo [OK]    제거 완료!
echo ==========================================
echo.
echo   보존된 파일:
echo     app.jar, config\, mapper\   [데이터 보호를 위해 유지]
echo.
echo   완전 삭제를 원하면 이 폴더 전체를 삭제하세요:
echo     %APP_DIR%
echo.
pause
endlocal

@echo off
:: ============================================================
:: uninstall.bat -- Application removal script (Windows)
:: ============================================================
:: Stops the running app and optionally cleans up logs/backups.
:: app.jar, config\, mapper\ are NOT deleted (data protection).
:: For complete removal, delete this entire folder manually.
:: ============================================================
setlocal EnableDelayedExpansion

:: ── Path resolution ──────────────────────────────────────────────────────────
pushd "%~dp0.."
set "APP_DIR=%CD%"
popd
set "PID_FILE=%APP_DIR%\app.pid"
set "CONFIG_FILE=%APP_DIR%\config\application.yml"

echo.
echo ==========================================
echo   Spring Starter Maven -- Uninstall
echo ==========================================
echo.
echo   This script will:
echo     [YES] Stop the running application
echo     [YES] Clean up PID file, logs, backups (optional)
echo     [NO]  app.jar, config\, mapper\ are kept (data protection)
echo.
set /p "_CONFIRM=  Proceed with uninstall? (y/N) : "
if /i not "!_CONFIRM!"=="y" (
    echo   Uninstall cancelled.
    pause & exit /b 0
)
echo.

:: ── Stop application ─────────────────────────────────────────────────────────
echo [INFO]  Checking for running application...

set "APP_PID="

if exist "%PID_FILE%" (
    set /p "APP_PID=" < "%PID_FILE%"
)

if "!APP_PID!"=="" (
    for /f "skip=1 tokens=2 delims= " %%p in (
        'wmic process where "commandline like \"%%app.jar%%\"" get processid 2^>nul'
    ) do (
        if not "%%p"=="" set "APP_PID=%%p"
    )
)

if not "!APP_PID!"=="" (
    echo [INFO]  Stopping application (PID: !APP_PID!)...
    taskkill /PID !APP_PID! /F >nul 2>&1
    if errorlevel 1 (
        echo [WARN]  Process not found. May already be stopped.
    ) else (
        echo [OK]    Application stopped.
    )
) else (
    echo [INFO]  No running application found.
)

if exist "%PID_FILE%" del /f "%PID_FILE%"

echo.

:: ── Optional cleanup ─────────────────────────────────────────────────────────
echo ----------------------------------------------------------
echo   Optional file cleanup
echo ----------------------------------------------------------

if exist "%APP_DIR%\logs\" (
    dir /b /a "%APP_DIR%\logs\" 2>nul | findstr . >nul
    if not errorlevel 1 (
        set /p "_DEL_LOGS=  Delete log files? (logs\) (y/N) : "
        if /i "!_DEL_LOGS!"=="y" (
            rmdir /s /q "%APP_DIR%\logs" 2>nul
            echo [OK]    Log directory deleted.
        ) else (
            echo [INFO]  Logs kept: %APP_DIR%\logs
        )
    ) else (
        echo [INFO]  Log directory is empty.
    )
)

if exist "%CONFIG_FILE%.bak" (
    set /p "_DEL_BAK=  Delete config backup? (config\application.yml.bak) (y/N) : "
    if /i "!_DEL_BAK!"=="y" (
        del /f "%CONFIG_FILE%.bak"
        echo [OK]    Config backup deleted.
    )
)

:: ── Summary ──────────────────────────────────────────────────────────────────
echo.
echo ==========================================
echo [OK]    Uninstall complete!
echo ==========================================
echo.
echo   Preserved files:
echo     app.jar, config\, mapper\
echo.
echo   For complete removal, delete this folder:
echo     %APP_DIR%
echo.
pause
endlocal

@echo off
:: ============================================================
:: install.bat -- First-time installation script (Windows)
:: ============================================================
setlocal EnableDelayedExpansion

:: ── Path resolution ──────────────────────────────────────────────────────────
pushd "%~dp0.."
set "APP_DIR=%CD%"
popd
set "CONFIG_FILE=%APP_DIR%\config\application.yml"
set "APP_JAR=%APP_DIR%\app.jar"

echo.
echo ==========================================
echo   Spring Starter Maven -- Installation
echo ==========================================
echo.

:: ── Pre-flight checks ────────────────────────────────────────────────────────
echo [INFO]  Checking environment...

if not exist "%APP_JAR%" (
    echo [ERROR] app.jar not found: %APP_JAR%
    pause & exit /b 1
)

if not exist "%CONFIG_FILE%" (
    echo [ERROR] config\application.yml not found: %CONFIG_FILE%
    pause & exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found in PATH. Please install JDK 17 or higher.
    pause & exit /b 1
)

for /f "tokens=3 delims= " %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "_JAVA_VER_RAW=%%v"
)
set "_JAVA_VER_RAW=!_JAVA_VER_RAW:"=!"
for /f "delims=." %%a in ("!_JAVA_VER_RAW!") do set "_JAVA_MAJOR=%%a"

if !_JAVA_MAJOR! LSS 17 (
    echo [WARN]  Java 17+ recommended. Detected: !_JAVA_VER_RAW!
) else (
    echo [OK]    Environment check passed. Java !_JAVA_VER_RAW!
)
echo.

:: ── Database connection input ────────────────────────────────────────────────
echo ----------------------------------------------------------
echo   Database connection setup
echo   (Press Enter to use the default value shown in brackets)
echo ----------------------------------------------------------
echo.

set "DB_HOST=localhost"
set /p "_INPUT=  DB Host         [localhost] : "
if not "!_INPUT!"=="" set "DB_HOST=!_INPUT!"

set "DB_PORT=3306"
set /p "_INPUT=  DB Port         [3306]      : "
if not "!_INPUT!"=="" set "DB_PORT=!_INPUT!"

set "DB_NAME=SJSJSS"
set /p "_INPUT=  DB Name         [SJSJSS]    : "
if not "!_INPUT!"=="" set "DB_NAME=!_INPUT!"

set "DB_USERNAME="
set /p "DB_USERNAME=  DB Username     : "
if "!DB_USERNAME!"=="" (
    echo [ERROR] DB Username is required.
    pause & exit /b 1
)

:: Password input -- masked via PowerShell Read-Host -AsSecureString
echo   DB Password (input will be hidden):
for /f "usebackq delims=" %%P in (
    `powershell -NoProfile -Command "[Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR((Read-Host -Prompt '  DB Password    ' -AsSecureString)))"`
) do set "DB_PASSWORD=%%P"

if "!DB_PASSWORD!"=="" (
    echo [ERROR] DB Password is required.
    pause & exit /b 1
)

echo.
set "APP_PORT=8080"
set /p "_INPUT=  App Port        [8080]      : "
if not "!_INPUT!"=="" set "APP_PORT=!_INPUT!"

echo.

:: ── Confirm settings ─────────────────────────────────────────────────────────
echo ----------------------------------------------------------
echo   Confirm settings
echo ----------------------------------------------------------
echo   DB URL      : jdbc:mariadb://!DB_HOST!:!DB_PORT!/!DB_NAME!
echo   DB Username : !DB_USERNAME!
echo   App Port    : !APP_PORT!
echo.
set /p "_CONFIRM=  Proceed with installation? (y/N) : "
if /i not "!_CONFIRM!"=="y" (
    echo   Installation cancelled.
    pause & exit /b 0
)
echo.

:: ── Update config\application.yml ────────────────────────────────────────────
echo [INFO]  Updating config\application.yml...

:: Backup
copy "%CONFIG_FILE%" "%CONFIG_FILE%.bak" >nul 2>&1
echo [INFO]  Backup created: %CONFIG_FILE%.bak

:: Pass values via environment variables to avoid special character escaping issues
set "PS_CONFIG=%CONFIG_FILE%"
set "PS_DB_HOST=%DB_HOST%"
set "PS_DB_PORT=%DB_PORT%"
set "PS_DB_NAME=%DB_NAME%"
set "PS_DB_USER=%DB_USERNAME%"
set "PS_DB_PASS=%DB_PASSWORD%"
set "PS_APP_PORT=%APP_PORT%"

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$cfg = $env:PS_CONFIG;" ^
    "$c = [IO.File]::ReadAllText($cfg, [Text.Encoding]::UTF8);" ^
    "$c = $c -replace 'url: jdbc:mariadb://[^\r\n ]+', ('url: jdbc:mariadb://' + $env:PS_DB_HOST + ':' + $env:PS_DB_PORT + '/' + $env:PS_DB_NAME);" ^
    "$c = $c -replace 'username: your_db_user', ('username: ' + $env:PS_DB_USER);" ^
    "$c = $c -replace 'password: your_db_password', ('password: ' + $env:PS_DB_PASS);" ^
    "$c = [Text.RegularExpressions.Regex]::Replace($c, '(?m)^(\s+port:\s*)\d+', '${1}' + $env:PS_APP_PORT);" ^
    "[IO.File]::WriteAllText($cfg, $c, [Text.Encoding]::UTF8);"

if errorlevel 1 (
    echo [ERROR] Failed to update config\application.yml. Please edit manually.
    pause & exit /b 1
)

echo [OK]    config\application.yml updated.
echo.

:: ── Create logs directory ─────────────────────────────────────────────────────
if not exist "%APP_DIR%\logs" mkdir "%APP_DIR%\logs"

:: ── Optional immediate start ──────────────────────────────────────────────────
set "_START_NOW=Y"
set /p "_INPUT=  Start the application now? (Y/n) : "
if not "!_INPUT!"=="" set "_START_NOW=!_INPUT!"

if /i "!_START_NOW!"=="y" (
    call "%APP_DIR%\bin\start.bat"
)

:: ── Summary ──────────────────────────────────────────────────────────────────
echo.
echo ==========================================
echo [OK]    Installation complete!
echo ==========================================
echo.
echo   App URL     : http://localhost:!APP_PORT!
echo   Log file    : %APP_DIR%\logs\application.log
echo   Config file : %CONFIG_FILE%
echo.
echo   Management commands:
echo     Start   : bin\start.bat
echo     Stop    : bin\stop.bat
echo     Remove  : bin\uninstall.bat
echo   --------------------------------------------------
echo.
pause
endlocal

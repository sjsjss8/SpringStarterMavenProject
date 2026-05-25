@echo off
:: ============================================================
:: install.bat — 최초 설치 스크립트 (Windows)
:: ============================================================
:: 역할:
::   ZIP 압축 해제 후 최초 1회 실행하는 자동 설치 스크립트.
::   대화형으로 DB 접속 정보를 입력받아 config\application.yml에
::   자동으로 반영하고, 앱을 선택적으로 즉시 시작한다.
::
:: 사용법:
::   bin\install.bat   (탐색기에서 더블클릭 또는 cmd에서 실행)
::
:: 주의:
::   이미 설치된 경우 재실행하면 기존 설정을 덮어씌움.
::   설정만 변경하려면 config\application.yml 을 직접 편집 후
::   bin\stop.bat → bin\start.bat
:: ============================================================
setlocal EnableDelayedExpansion
chcp 65001 >nul 2>&1

:: ── 경로 계산 ────────────────────────────────────────────────────────────────
pushd "%~dp0.."
set "APP_DIR=%CD%"
popd
set "CONFIG_FILE=%APP_DIR%\config\application.yml"
set "APP_JAR=%APP_DIR%\app.jar"

echo.
echo ==========================================
echo   Spring Starter Maven -- 설치 시작
echo ==========================================
echo.

:: ── 사전 검사 ───────────────────────────────────────────────────────────────
echo [INFO]  환경 확인 중...

if not exist "%APP_JAR%" (
    echo [ERROR] app.jar 를 찾을 수 없습니다: %APP_JAR%
    pause & exit /b 1
)

if not exist "%CONFIG_FILE%" (
    echo [ERROR] config\application.yml 을 찾을 수 없습니다: %CONFIG_FILE%
    pause & exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java 가 설치되지 않았거나 PATH 에 없습니다. JDK 17 이상을 설치하세요.
    pause & exit /b 1
)

:: Java 버전 추출
for /f "tokens=3 delims= " %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "_JAVA_VER_RAW=%%v"
)
set "_JAVA_VER_RAW=!_JAVA_VER_RAW:"=!"
for /f "delims=." %%a in ("!_JAVA_VER_RAW!") do set "_JAVA_MAJOR=%%a"

if !_JAVA_MAJOR! LSS 17 (
    echo [WARN]  Java 17 이상을 권장합니다. (감지된 버전: !_JAVA_VER_RAW!)
) else (
    echo [OK]    환경 확인 완료 (Java !_JAVA_VER_RAW!)
)
echo.

:: ── DB 접속 정보 입력 ────────────────────────────────────────────────────────
echo -- 데이터베이스 접속 정보를 입력하세요 ---------------------------
echo    (기본값이 있는 항목은 Enter 를 누르면 기본값이 사용됩니다)
echo.

set "DB_HOST=localhost"
set /p "_INPUT=  DB 호스트       [localhost] : "
if not "!_INPUT!"=="" set "DB_HOST=!_INPUT!"

set "DB_PORT=3306"
set /p "_INPUT=  DB 포트         [3306]      : "
if not "!_INPUT!"=="" set "DB_PORT=!_INPUT!"

set "DB_NAME=SJSJSS"
set /p "_INPUT=  DB 이름         [SJSJSS]    : "
if not "!_INPUT!"=="" set "DB_NAME=!_INPUT!"

set "DB_USERNAME="
set /p "DB_USERNAME=  DB 계정         : "
if "!DB_USERNAME!"=="" (
    echo [ERROR] DB 계정은 필수 입력값입니다.
    pause & exit /b 1
)

:: 비밀번호: PowerShell Read-Host -AsSecureString 으로 화면 마스킹
echo   DB 비밀번호 (입력 시 화면에 표시되지 않습니다):
for /f "usebackq delims=" %%P in (
    `powershell -NoProfile -Command "[Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR((Read-Host -Prompt '  DB 비밀번호     ' -AsSecureString)))"`
) do set "DB_PASSWORD=%%P"

if "!DB_PASSWORD!"=="" (
    echo [ERROR] DB 비밀번호는 필수 입력값입니다.
    pause & exit /b 1
)

echo.
set "APP_PORT=8080"
set /p "_INPUT=  앱 포트         [8080]      : "
if not "!_INPUT!"=="" set "APP_PORT=!_INPUT!"

echo.

:: ── 입력값 확인 ──────────────────────────────────────────────────────────────
echo -- 입력 내용 확인 -----------------------------------------------
echo    DB URL  : jdbc:mariadb://!DB_HOST!:!DB_PORT!/!DB_NAME!
echo    DB 계정 : !DB_USERNAME!
echo    앱 포트 : !APP_PORT!
echo.
set /p "_CONFIRM=  위 내용으로 설치를 진행하시겠습니까? (y/N) : "
if /i not "!_CONFIRM!"=="y" (
    echo   설치를 취소합니다.
    pause & exit /b 0
)
echo.

:: ── config\application.yml 업데이트 ──────────────────────────────────────────
echo [INFO]  config\application.yml 설정 중...

:: 백업 생성
copy "%CONFIG_FILE%" "%CONFIG_FILE%.bak" >nul 2>&1
echo [INFO]  백업 파일 생성: %CONFIG_FILE%.bak

:: 환경변수로 PowerShell 에 값 전달 (특수문자 이스케이프 문제 회피)
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
    echo [ERROR] config\application.yml 업데이트 실패. 수동으로 편집하세요.
    pause & exit /b 1
)

echo [OK]    config\application.yml 설정 완료
echo.

:: ── 로그 디렉터리 생성 ───────────────────────────────────────────────────────
if not exist "%APP_DIR%\logs" mkdir "%APP_DIR%\logs"

:: ── 앱 시작 (선택) ───────────────────────────────────────────────────────────
set "_START_NOW=Y"
set /p "_INPUT=지금 바로 앱을 시작하시겠습니까? (Y/n) : "
if not "!_INPUT!"=="" set "_START_NOW=!_INPUT!"

if /i "!_START_NOW!"=="y" (
    call "%APP_DIR%\bin\start.bat"
)

:: ── 설치 완료 요약 ────────────────────────────────────────────────────────────
echo.
echo ==========================================
echo [OK]    설치 완료!
echo ==========================================
echo.
echo   앱 URL    : http://localhost:!APP_PORT!
echo   로그 경로 : %APP_DIR%\logs\application.log
echo   설정 파일 : %CONFIG_FILE%
echo.
echo   -- 이후 관리 명령어 ----------------------------------
echo   시작  : bin\start.bat
echo   종료  : bin\stop.bat
echo   -------------------------------------------------------
echo.
pause
endlocal

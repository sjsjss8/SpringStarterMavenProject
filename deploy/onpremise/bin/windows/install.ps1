# ============================================================
# install.ps1 -- First-time installation script (Windows)
# ============================================================
# Usage: Run install.bat (which calls this script)
#        Or directly: powershell -ExecutionPolicy Bypass -File bin\windows\install.ps1
# ============================================================

$ErrorActionPreference = "Stop"

# ── Path resolution ───────────────────────────────────────────────────────────
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$AppDir    = Split-Path -Parent (Split-Path -Parent $ScriptDir)   # bin\windows → bin → app root
$ConfigFile = Join-Path $AppDir "config\application.yml"
$AppJar     = Join-Path $AppDir "app.jar"

Write-Host ""
Write-Host "=========================================="
Write-Host "  Spring Starter Maven -- Installation"
Write-Host "=========================================="
Write-Host ""

# ── Pre-flight checks ─────────────────────────────────────────────────────────
Write-Host "[INFO]  Checking environment..."

if (-not (Test-Path $AppJar)) {
    Write-Host "[ERROR] app.jar not found: $AppJar"
    exit 1
}

if (-not (Test-Path $ConfigFile)) {
    Write-Host "[ERROR] config\application.yml not found: $ConfigFile"
    exit 1
}

# java 실행 파일 찾기: PATH → JAVA_HOME\bin 순으로 탐색
$javaExe = $null
if (Get-Command java -ErrorAction SilentlyContinue) {
    $javaExe = "java"
} elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaExe = "$env:JAVA_HOME\bin\java.exe"
}

if (-not $javaExe) {
    Write-Host "[ERROR] Java not found in PATH or JAVA_HOME."
    Write-Host "        Please install JDK 17+ and set JAVA_HOME or add java to PATH."
    exit 1
}

try {
    $javaVersion = (& $javaExe -version 2>&1 | Select-String "version") -replace '.*"([\d.]+)".*', '$1'
    $javaMajor   = [int]($javaVersion -split '\.')[0]
    if ($javaMajor -lt 17) {
        Write-Host "[WARN]  Java 17+ recommended. Detected: $javaVersion"
    } else {
        Write-Host "[OK]    Environment check passed. Java $javaVersion"
    }
} catch {
    Write-Host "[WARN]  Could not determine Java version, continuing..."
}
Write-Host ""

# ── Database connection input ─────────────────────────────────────────────────
Write-Host "----------------------------------------------------------"
Write-Host "  Database connection setup"
Write-Host "  (Press Enter to use the default value shown in brackets)"
Write-Host "----------------------------------------------------------"
Write-Host ""

$input    = Read-Host "  DB Host         [localhost]"
$DbHost   = if ($input) { $input } else { "localhost" }

$input    = Read-Host "  DB Port         [3306]"
$DbPort   = if ($input) { $input } else { "3306" }

$input    = Read-Host "  DB Name         [SJSJSS]"
$DbName   = if ($input) { $input } else { "SJSJSS" }

$DbUsername = Read-Host "  DB Username"
if (-not $DbUsername) {
    Write-Host "[ERROR] DB Username is required."
    exit 1
}

$secPass = Read-Host "  DB Password" -AsSecureString
$DbPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secPass)
)
if (-not $DbPassword) {
    Write-Host "[ERROR] DB Password is required."
    exit 1
}

Write-Host ""
$input   = Read-Host "  App Port        [8080]"
$AppPort = if ($input) { $input } else { "8080" }

Write-Host ""

# ── Confirm ───────────────────────────────────────────────────────────────────
Write-Host "----------------------------------------------------------"
Write-Host "  Confirm settings"
Write-Host "----------------------------------------------------------"
Write-Host "  DB URL      : jdbc:mariadb://${DbHost}:${DbPort}/${DbName}"
Write-Host "  DB Username : $DbUsername"
Write-Host "  App Port    : $AppPort"
Write-Host ""
$confirm = Read-Host "  Proceed with installation? (y/N)"
if ($confirm -notmatch '^[Yy]$') {
    Write-Host "  Installation cancelled."
    exit 0
}
Write-Host ""

# ── Update config\application.yml ────────────────────────────────────────────
Write-Host "[INFO]  Updating config\application.yml..."

Copy-Item $ConfigFile "$ConfigFile.bak" -Force
Write-Host "[INFO]  Backup created: $ConfigFile.bak"

$content = [IO.File]::ReadAllText($ConfigFile, [Text.Encoding]::UTF8)

$content = $content -replace 'url: jdbc:mariadb://[^\r\n ]+',
    "url: jdbc:mariadb://${DbHost}:${DbPort}/${DbName}"

$content = $content -replace 'username: your_db_user',
    "username: $DbUsername"

$content = $content -replace 'password: your_db_password',
    "password: $DbPassword"

$content = [Text.RegularExpressions.Regex]::Replace(
    $content, '(?m)^(\s+port:\s*)\d+', '${1}' + $AppPort
)

[IO.File]::WriteAllText($ConfigFile, $content, [Text.Encoding]::UTF8)

Write-Host "[OK]    config\application.yml updated."
Write-Host ""

# ── Create logs directory ─────────────────────────────────────────────────────
$logsDir = Join-Path $AppDir "logs"
if (-not (Test-Path $logsDir)) { New-Item -ItemType Directory $logsDir | Out-Null }

# ── Optional immediate start ──────────────────────────────────────────────────
$startNow = Read-Host "  Start the application now? (Y/n)"
if ($startNow -match '^[Yy]$' -or $startNow -eq '') {
    $startBat = Join-Path $ScriptDir "start.bat"
    if (Test-Path $startBat) {
        & cmd /c $startBat
    }
}

# ── Summary ───────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "=========================================="
Write-Host "[OK]    Installation complete!"
Write-Host "=========================================="
Write-Host ""
Write-Host "  App URL     : http://localhost:$AppPort"
Write-Host "  Log file    : $logsDir\application.log"
Write-Host "  Config file : $ConfigFile"
Write-Host ""
Write-Host "  Management commands:"
Write-Host "    Start   : bin\windows\start.bat"
Write-Host "    Stop    : bin\windows\stop.bat"
Write-Host "    Remove  : bin\windows\uninstall.bat"
Write-Host "  --------------------------------------------------"
Write-Host ""

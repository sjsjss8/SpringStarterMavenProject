# ============================================================
# start.ps1 -- Application start script (Windows)
# ============================================================

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$AppDir    = Split-Path -Parent $ScriptDir
$AppJar    = Join-Path $AppDir "app.jar"
$LogDir    = Join-Path $AppDir "logs"
$PidFile   = Join-Path $AppDir "app.pid"

# ── Find Java ─────────────────────────────────────────────────────────────────
$javaExe = $null
if (Get-Command java -ErrorAction SilentlyContinue) {
    $javaExe = "java"
} elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaExe = "$env:JAVA_HOME\bin\java.exe"
}

if (-not $javaExe) {
    Write-Host "[ERROR] Java not found in PATH or JAVA_HOME."
    Write-Host "        Please install JDK 17+ and set JAVA_HOME."
    exit 1
}

# ── Pre-flight check ──────────────────────────────────────────────────────────
if (-not (Test-Path $AppJar)) {
    Write-Host "[ERROR] app.jar not found: $AppJar"
    exit 1
}

# ── Duplicate run check ───────────────────────────────────────────────────────
if (Test-Path $PidFile) {
    $existingPid = (Get-Content $PidFile -Raw).Trim()
    $proc = Get-Process -Id $existingPid -ErrorAction SilentlyContinue
    if ($proc) {
        Write-Host "[WARN]  Application is already running (PID: $existingPid)."
        Write-Host "        Run stop.bat first to restart."
        exit 1
    } else {
        Write-Host "[INFO]  Stale PID file found, cleaning up."
        Remove-Item $PidFile -Force
    }
}

# ── Create logs directory ─────────────────────────────────────────────────────
if (-not (Test-Path $LogDir)) { New-Item -ItemType Directory $LogDir | Out-Null }

$logFile  = Join-Path $LogDir "application.log"
$startLog = Join-Path $LogDir "start.log"

# ── JVM options ───────────────────────────────────────────────────────────────
$jvmOpts = @("-Xms256m", "-Xmx1g")

# ── Start application ─────────────────────────────────────────────────────────
Write-Host "=========================================="
Write-Host "  Spring Starter Maven -- Starting"
Write-Host "=========================================="
Write-Host "  App dir  : $AppDir"
Write-Host "  Config   : $AppDir\config\application.yml"
Write-Host "  Log      : $logFile"
Write-Host "  Java     : $( & $javaExe -version 2>&1 | Select-Object -First 1 )"
Write-Host "=========================================="
Write-Host ""

$proc = Start-Process -FilePath $javaExe `
    -ArgumentList ($jvmOpts + @("-jar", "`"$AppJar`"")) `
    -WorkingDirectory $AppDir `
    -RedirectStandardOutput $logFile `
    -RedirectStandardError  $startLog `
    -NoNewWindow -PassThru

$proc.Id | Out-File $PidFile -Encoding ASCII

Write-Host "[OK]    Application started (PID: $($proc.Id))"
Write-Host "        Ready in ~30-60 seconds."
Write-Host ""
Write-Host "  Health check : http://localhost:8080/actuator/health"
Write-Host "  Log          : $logFile"
Write-Host ""

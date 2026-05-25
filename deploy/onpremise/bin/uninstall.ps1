# ============================================================
# uninstall.ps1 -- Application removal script (Windows)
# ============================================================
# Stops the running app and optionally cleans up logs/backups.
# app.jar, config\, mapper\ are NOT deleted (data protection).
# ============================================================

$ErrorActionPreference = "SilentlyContinue"

$ScriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$AppDir     = Split-Path -Parent $ScriptDir
$PidFile    = Join-Path $AppDir "app.pid"
$ConfigFile = Join-Path $AppDir "config\application.yml"

Write-Host ""
Write-Host "=========================================="
Write-Host "  Spring Starter Maven -- Uninstall"
Write-Host "=========================================="
Write-Host ""
Write-Host "  This script will:"
Write-Host "    [YES] Stop the running application"
Write-Host "    [YES] Clean up PID file, logs, backups (optional)"
Write-Host "    [NO]  app.jar, config\, mapper\ are kept (data protection)"
Write-Host ""
$confirm = Read-Host "  Proceed with uninstall? (y/N)"
if ($confirm -notmatch '^[Yy]$') {
    Write-Host "  Uninstall cancelled."
    exit 0
}
Write-Host ""

# ── Stop application ──────────────────────────────────────────────────────────
Write-Host "[INFO]  Checking for running application..."

$appPid = $null

if (Test-Path $PidFile) {
    $appPid = (Get-Content $PidFile -Raw).Trim()
}

if (-not $appPid) {
    $proc = Get-WmiObject Win32_Process | Where-Object { $_.CommandLine -like '*app.jar*' }
    if ($proc) { $appPid = $proc.ProcessId }
}

if ($appPid) {
    Write-Host "[INFO]  Stopping application (PID: $appPid)..."
    Stop-Process -Id $appPid -Force -ErrorAction SilentlyContinue
    Write-Host "[OK]    Application stopped."
} else {
    Write-Host "[INFO]  No running application found."
}

if (Test-Path $PidFile) { Remove-Item $PidFile -Force }

Write-Host ""

# ── Optional cleanup ──────────────────────────────────────────────────────────
Write-Host "----------------------------------------------------------"
Write-Host "  Optional file cleanup"
Write-Host "----------------------------------------------------------"

$logsDir = Join-Path $AppDir "logs"
if (Test-Path $logsDir) {
    $hasFiles = (Get-ChildItem $logsDir -Recurse | Measure-Object).Count -gt 0
    if ($hasFiles) {
        $del = Read-Host "  Delete log files? (logs\) (y/N)"
        if ($del -match '^[Yy]$') {
            Remove-Item $logsDir -Recurse -Force
            Write-Host "[OK]    Log directory deleted."
        } else {
            Write-Host "[INFO]  Logs kept: $logsDir"
        }
    } else {
        Write-Host "[INFO]  Log directory is empty."
    }
}

if (Test-Path "$ConfigFile.bak") {
    $del = Read-Host "  Delete config backup? (config\application.yml.bak) (y/N)"
    if ($del -match '^[Yy]$') {
        Remove-Item "$ConfigFile.bak" -Force
        Write-Host "[OK]    Config backup deleted."
    }
}

# ── Summary ───────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "=========================================="
Write-Host "[OK]    Uninstall complete!"
Write-Host "=========================================="
Write-Host ""
Write-Host "  Preserved files:"
Write-Host "    app.jar, config\, mapper\"
Write-Host ""
Write-Host "  For complete removal, delete this folder:"
Write-Host "    $AppDir"
Write-Host ""

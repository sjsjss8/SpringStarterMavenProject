# ============================================================
# stop.ps1 -- Application stop script (Windows)
# ============================================================

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$AppDir    = Split-Path -Parent $ScriptDir
$PidFile   = Join-Path $AppDir "app.pid"

Write-Host "=========================================="
Write-Host "  Spring Starter Maven -- Stopping"
Write-Host "=========================================="

if (-not (Test-Path $PidFile)) {
    Write-Host "[WARN]  PID file not found: $PidFile"
    Write-Host "        Application may not be running, or was not started via start.bat."
    Write-Host ""
    Write-Host "  Running java.exe processes:"
    Get-Process java -ErrorAction SilentlyContinue | Format-Table Id, CPU, StartTime -AutoSize
    Write-Host ""
    Write-Host "  To stop manually: Stop-Process -Name java -Force"
    exit 1
}

$appPid = (Get-Content $PidFile -Raw).Trim()
$proc   = Get-Process -Id $appPid -ErrorAction SilentlyContinue

if (-not $proc) {
    Write-Host "[WARN]  Process (PID: $appPid) not found. Already stopped."
    Remove-Item $PidFile -Force
    exit 0
}

Write-Host "[INFO]  Stopping application (PID: $appPid)..."
Stop-Process -Id $appPid -Force
Remove-Item $PidFile -Force

Write-Host "[OK]    Application stopped (PID: $appPid)."
Write-Host ""

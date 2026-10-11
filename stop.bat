@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   GovFix - Stopping Server on Port 8080
echo ============================================================

set "FOUND=0"

:: Find PID listening on port 8080
for /f "tokens=5" %%a in ('netstat -aon ^| findstr /r ":8080 .*LISTENING"') do (
    set "PID=%%a"
    if not "!PID!"=="0" (
        echo [INFO] Found process with PID: !PID! on port 8080. Terminating...
        taskkill /F /PID !PID! >nul 2>&1
        if %ERRORLEVEL% equ 0 (
            echo [SUCCESS] GovFix process !PID! stopped successfully.
            set "FOUND=1"
        ) else (
            echo [WARNING] Could not terminate PID !PID!. It might have already exited.
        )
    )
)

if "!FOUND!"=="0" (
    echo [INFO] No active GovFix process found listening on port 8080.
)

echo.
echo GovFix stop routine finished.
echo.

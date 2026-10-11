@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   GovFix - Pimpri Chinchwad Civic Platform Launcher
echo   Java 21 + Spring Boot 3 + MySQL 8 + Leaflet GPS Tracker
echo ============================================================

:: 1. Verify / Set JAVA_HOME for Java 21
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot" (
        set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
    )
)

if not "%JAVA_HOME%"=="" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

:: Verify Java installation
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java 21 is not found. Please install Java 21 or set JAVA_HOME.
    pause
    exit /b 1
)

:: 2. Check if JAR is built; if not, compile with Maven
if not exist "target\govfix-1.0.0.jar" (
    echo [INFO] target\govfix-1.0.0.jar not found. Building project with Maven...
    if exist "D:\tools\apache-maven-3.9.9\bin\mvn.cmd" (
        call "D:\tools\apache-maven-3.9.9\bin\mvn.cmd" clean package -DskipTests
    ) else (
        call mvn clean package -DskipTests
    )
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Maven build failed.
        pause
        exit /b 1
    )
)

:: 3. Launch the GovFix Spring Boot Application
echo.
echo [INFO] Starting GovFix application on http://localhost:8080 ...
echo [INFO] Press Ctrl+C or run stop.bat in another terminal to stop the server.
echo.

java -jar target\govfix-1.0.0.jar

pause

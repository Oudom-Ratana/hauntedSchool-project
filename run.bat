@echo off
setlocal
title Khmer Spirit: The Haunted School 3

echo ===================================================
echo     Khmer Spirit: The Haunted School 3
echo ===================================================
echo.

:: 1. Check if Java is installed
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java was not found on your system!
    echo.
    echo Please install Java 21 or higher to play this game.
    echo Download: https://www.oracle.com/java/technologies/downloads/
    echo.
    pause
    exit /b 1
)

:: 2. Check and launch using automation suite if JAR is missing or flags passed
if "%1"=="--build" (
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0automate.ps1" -Build
    exit /b %ERRORLEVEL%
)
if "%1"=="--auto" (
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0automate.ps1" -Auto
    exit /b %ERRORLEVEL%
)
if "%1"=="--menu" (
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0automate.ps1"
    exit /b %ERRORLEVEL%
)

if not exist "hantedSchool_3.jar" (
    echo [INFO] Game JAR not found. Running complete automation suite to build...
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0automate.ps1" -Auto
    exit /b %ERRORLEVEL%
)

:: 3. Launch the game
echo [INFO] Launching the game...
echo.
java --enable-native-access=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED -jar "hantedSchool_3.jar"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [NOTE] Game process finished.
    pause
)


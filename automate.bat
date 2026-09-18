@echo off
setlocal
title Khmer Spirit: The Haunted School 3 - Automation Suite
cd /d "%~dp0"

:: Pass all arguments directly to the PowerShell automation suite with execution bypass
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0automate.ps1" %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Automation process returned an error code.
    pause
)

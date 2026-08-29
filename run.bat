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

:: 2. Locate the runnable JAR file
if exist "hantedSchool_3.jar" (
    set "JAR_PATH=hantedSchool_3.jar"
) else if exist "target\hantedSchool_3.jar" (
    set "JAR_PATH=target\hantedSchool_3.jar"
) else (
    echo [INFO] Game JAR not found. Attempting to build using Maven...
    mvn clean package
    if exist "target\hantedSchool_3.jar" (
        set "JAR_PATH=target\hantedSchool_3.jar"
    ) else (
        echo.
        echo [ERROR] Could not find or build hantedSchool_3.jar!
        echo.
        pause
        exit /b 1
    )
)

:: 3. Launch the game
echo [INFO] Launching the game...
echo.
java --enable-native-access=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED -jar "%JAR_PATH%"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [NOTE] Game process finished.
    pause
)

@echo off
setlocal EnableExtensions

set "GRADLE_VERSION=9.2.1"
set "PROJECT_DIR=%~dp0"
set "BOOTSTRAP_DIR=%PROJECT_DIR%.gradle-bootstrap"
set "GRADLE_HOME=%BOOTSTRAP_DIR%\gradle-%GRADLE_VERSION%"
set "GRADLE_BAT=%GRADLE_HOME%\bin\gradle.bat"
set "GRADLE_ZIP=%BOOTSTRAP_DIR%\gradle-%GRADLE_VERSION%-bin.zip"

if not exist "%GRADLE_BAT%" (
    echo [killeffect] Gradle %GRADLE_VERSION% not found. Downloading it once...
    if not exist "%BOOTSTRAP_DIR%" mkdir "%BOOTSTRAP_DIR%"

    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
      "$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%GRADLE_ZIP%'; Expand-Archive -Force '%GRADLE_ZIP%' '%BOOTSTRAP_DIR%'"

    if errorlevel 1 (
        echo [killeffect] Failed to download or extract Gradle.
        exit /b 1
    )

    del /q "%GRADLE_ZIP%" >nul 2>&1
)

call "%GRADLE_BAT%" %*
exit /b %ERRORLEVEL%

@echo off
setlocal EnableExtensions
set "GRADLE_VERSION=8.14"
set "BOOT_DIR=%~dp0.gradle-bootstrap"
set "GRADLE_HOME=%BOOT_DIR%\gradle-%GRADLE_VERSION%"
set "ZIP=%BOOT_DIR%\gradle-%GRADLE_VERSION%-bin.zip"

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    if not exist "%BOOT_DIR%" mkdir "%BOOT_DIR%"
    echo Downloading Gradle %GRADLE_VERSION%...
    powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP%'; Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%BOOT_DIR%' -Force"
    if errorlevel 1 exit /b 1
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%

@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ==================================================
echo Glitched Crown - Dragonfyre Forge 1.20.1 build
echo ==================================================
echo.
echo Java 17 or newer and an internet connection are required.
echo The first build downloads Gradle, Forge, and Curios.
echo.

if exist build-log.txt del /q build-log.txt
call gradlew.bat clean build --stacktrace > build-log.txt 2>&1
set "RESULT=%ERRORLEVEL%"
type build-log.txt

echo.
if "%RESULT%"=="0" (
    echo ==================================================
    echo BUILD SUCCESSFUL
    echo Output folder: %CD%\build\libs
    echo ==================================================
    if exist "%CD%\build\libs" explorer "%CD%\build\libs"
) else (
    echo ==================================================
    echo BUILD FAILED - error code %RESULT%
    echo Log file: %CD%\build-log.txt
    echo ==================================================
)

echo.
pause
exit /b %RESULT%

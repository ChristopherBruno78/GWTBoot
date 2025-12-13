@echo off
REM GWT Boot CLI Installer for Windows
REM Installs the Java CLI tool and wrapper scripts

echo ===================================
echo GWT Boot CLI Installer
echo ===================================
echo.

REM Check if running from correct directory
if not exist "pom.xml" (
    echo Error: Installation must be run from the cli-tool directory
    exit /b 1
)

REM Install archetype to local Maven repository
echo Installing GWT Boot archetype to Maven repository...
echo.

cd ..
call mvn clean install

if errorlevel 1 (
    echo.
    echo Error: Failed to install archetype to Maven repository
    exit /b 1
)

echo.
echo Archetype installed to Maven repository
echo.

cd cli-tool

REM Build the CLI JAR
echo Building GWT Boot CLI JAR...
echo.

call mvn clean package

if errorlevel 1 (
    echo.
    echo Error: Failed to build GWT Boot CLI
    exit /b 1
)

if not exist "target\gwt-boot-cli.jar" (
    echo Error: JAR file not found after build
    exit /b 1
)

echo.
echo JAR built successfully
echo.

REM Set installation directories
set LIB_DIR=%USERPROFILE%\AppData\Local\gwt-boot\lib
set BIN_DIR=%USERPROFILE%\AppData\Local\gwt-boot\bin

echo Installing to user-local location:
echo   JAR: %LIB_DIR%
echo   Scripts: %BIN_DIR%
echo.

REM Check for existing installations
set WILL_OVERWRITE=false

if exist "%LIB_DIR%\gwt-boot-cli.jar" (
    set WILL_OVERWRITE=true
)
if exist "%BIN_DIR%\gwt-boot.bat" (
    set WILL_OVERWRITE=true
)
if exist "%BIN_DIR%\gwt-boot.ps1" (
    set WILL_OVERWRITE=true
)

REM Warn if files will be overwritten
if "%WILL_OVERWRITE%"=="true" (
    echo WARNING: The following files will be overwritten:
    if exist "%LIB_DIR%\gwt-boot-cli.jar" echo   - %LIB_DIR%\gwt-boot-cli.jar
    if exist "%BIN_DIR%\gwt-boot.bat" echo   - %BIN_DIR%\gwt-boot.bat
    if exist "%BIN_DIR%\gwt-boot.ps1" echo   - %BIN_DIR%\gwt-boot.ps1
    echo.
    set /p CONFIRM="Continue with installation? (y/N): "
    if /i not "%CONFIRM%"=="y" (
        echo Installation cancelled.
        exit /b 0
    )
    echo.
)

REM Create directories
if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

REM Copy JAR
echo Installing JAR file...
copy /y "target\gwt-boot-cli.jar" "%LIB_DIR%\gwt-boot-cli.jar" >nul
echo JAR installed to %LIB_DIR%\gwt-boot-cli.jar
echo.

REM Create batch wrapper
echo Creating Windows batch script...
(
echo @echo off
echo java -jar "%%~dp0\..\lib\gwt-boot-cli.jar" %%*
) > "%BIN_DIR%\gwt-boot.bat"

REM Create PowerShell wrapper
(
echo #!/usr/bin/env pwsh
echo $LIB_DIR = Join-Path $PSScriptRoot "..\lib"
echo $JAR_FILE = Join-Path $LIB_DIR "gwt-boot-cli.jar"
echo ^& java -jar $JAR_FILE $args
) > "%BIN_DIR%\gwt-boot.ps1"

echo Windows scripts installed:
echo   - %BIN_DIR%\gwt-boot.bat
echo   - %BIN_DIR%\gwt-boot.ps1
echo.

echo ===================================
echo Installation Complete!
echo ===================================
echo.

echo Successfully installed:
echo   - GWT Boot Archetype: %%USERPROFILE%%\.m2\repository\com\edusoftwerks\gwt-boot-archetype\1.0.0\
echo   - GWT Boot CLI JAR: %LIB_DIR%\gwt-boot-cli.jar
echo   - GWT Boot CLI Scripts: %BIN_DIR%\gwt-boot.bat and gwt-boot.ps1
echo.

REM Check if BIN_DIR is in PATH
echo %PATH% | findstr /C:"%BIN_DIR%" >nul
if errorlevel 1 (
    echo WARNING: %BIN_DIR% is not in your PATH
    echo.
    echo To add it permanently, run this command as Administrator:
    echo   setx PATH "%%PATH%%;%BIN_DIR%"
    echo.
    echo Or add it temporarily for this session:
    echo   set PATH=%%PATH%%;%BIN_DIR%
    echo.
) else (
    echo GWT Boot CLI is ready to use:
    echo   gwt-boot version
    echo.
    echo Get started:
    echo   gwt-boot boot myapp
    echo.
)

pause

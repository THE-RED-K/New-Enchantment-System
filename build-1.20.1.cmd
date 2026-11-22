@echo off
setlocal
rem ================================================================
rem  New Enchantment System 1.20.1 (Forge) - one-click build helper
rem
rem  Migrated from New Armor System / build-1.20.1.cmd
rem
rem  What it does:
rem    1. locates a local JDK 17 (1.20.1 targets Java 17)
rem    2. runs Gradle with daemon enabled + offline mode + local assets
rem
rem  Usage:
rem    build-1.20.1.cmd                 -> gradlew build --offline
rem    build-1.20.1.cmd compileJava     -> gradlew compileJava --offline
rem    build-1.20.1.cmd runClient       -> gradlew runClient --offline
rem ================================================================

set "PROJECT=%~dp0"

rem ---- locate JDK 17 -------------------------------------------------
rem  Known local installs first, then a generic scan under .gradle\jdks.
set "JDK17=C:\Users\Illus\.gradle\jdks\eclipse_adoptium-17-amd64-windows.2"
if not exist "%JDK17%\bin\java.exe" set "JDK17="
if not defined JDK17 set "JDK17=C:\Users\Illus\.gradle\jdks\eclipse_adoptium-17-amd64-windows\jdk-17.0.19+10"
if not exist "%JDK17%\bin\java.exe" set "JDK17="

if not defined JDK17 (
  for /d %%d in ("%USERPROFILE%\.gradle\jdks\*17*") do (
    if exist "%%d\bin\java.exe" set "JDK17=%%d"
    for /d %%e in ("%%d\*") do if exist "%%e\bin\java.exe" set "JDK17=%%e"
  )
)

if not defined JDK17 (
  echo [ERROR] JDK 17 not found under "%USERPROFILE%\.gradle\jdks"
  echo         Install one, or set JAVA_HOME manually before calling this script.
  exit /b 1
)

set "JAVA_HOME=%JDK17%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [env] JAVA_HOME=%JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version

rem ---- build ---------------------------------------------------------
cd /d "%PROJECT%"
if not exist "%PROJECT%\gradlew.bat" (
  echo [ERROR] gradlew.bat not found in "%PROJECT%"
  exit /b 1
)
if "%~1"=="" (
  echo [build] gradlew build --offline
  call gradlew.bat build --offline
) else (
  echo [build] gradlew %* --offline
  call gradlew.bat %* --offline
)
exit /b %ERRORLEVEL%

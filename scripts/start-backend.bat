@echo off
title Freenote - Backend Spring Boot
REM Les scripts sont dans scripts/ ; on se replace a la racine du projet.
cd /d "%~dp0.."

echo.
echo  ========================================
echo   Freenote - Backend Spring Boot
echo  ========================================
echo.
echo  Demarrage sur http://localhost:8080
echo.
echo  Prerequis: Docker doit etre lance (start-docker.bat)
echo.
echo  ========================================
echo.

call .\gradlew bootRun --args="--spring.profiles.active=dev"
pause

@echo off
title Freenote - Backend Spring Boot
cd /d "%~dp0"

echo.
echo  ========================================
echo   Freenote - Backend Spring Boot
echo  ========================================
echo.
echo  Demarrage sur http://localhost:8080
echo  Swagger UI   http://localhost:8080/swagger-ui.html
echo.
echo  Prerequis: Docker doit etre lance (start-docker.bat)
echo.
echo  ========================================
echo.

call .\gradlew bootRun --args="--spring.profiles.active=dev"
pause

@echo off
title Freenote - Dev Environment
cd /d "%~dp0"

echo.
echo  ========================================
echo   Freenote - Environnement de dev
echo  ========================================
echo.
echo  Backend     http://localhost:8080
echo  Swagger     http://localhost:8080/swagger-ui.html
echo  Frontend    http://localhost:3000
echo.
echo  Login dev   POST http://localhost:8080/api/dev/login/admin
echo              (remplacer admin par un username du seed)
echo.
echo  ========================================
echo.

echo  [0/3] Liberation des ports...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080.*LISTENING" 2^>nul') do (
    echo         Arret du processus %%a sur le port 8080
    taskkill /F /PID %%a >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":3000.*LISTENING" 2^>nul') do (
    echo         Arret du processus %%a sur le port 3000
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo  [1/3] Demarrage Docker...
docker compose up -d
if %errorlevel% neq 0 (
    echo.
    echo  [ERREUR] Docker n'est pas lance.
    pause
    exit /b 1
)

echo.
echo  [2/3] Demarrage du backend Spring Boot...
echo         (le seed s'execute au premier lancement)
start "Freenote Backend" cmd /c ".\gradlew bootRun --args="--spring.profiles.active=dev" & pause"

echo.
echo  [3/3] Demarrage du frontend React...
start "Freenote Frontend" cmd /c "cd frontend && npm install && npm run dev & pause"

echo.
echo  Tous les services demarrent en arriere-plan.
echo  Attends ~10s que le backend soit pret.
echo.
pause

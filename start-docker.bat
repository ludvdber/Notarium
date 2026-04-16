@echo off
title Freenote - Docker Infrastructure
cd /d "%~dp0"

echo.
echo  ========================================
echo   Freenote - Infrastructure Docker
echo  ========================================
echo.
echo  Services:
echo    PostgreSQL 17 + pgvector  localhost:5432
echo    Redis 7                   localhost:6379
echo    MinIO (S3)                localhost:9000
echo    MinIO Console             localhost:9001
echo    Meilisearch               localhost:7700
echo.
echo  Credentials:
echo    PostgreSQL  freenote / freenote / freenote
echo    MinIO       minioadmin / minioadmin
echo    Meilisearch dev-master-key
echo.
echo  ========================================
echo.

docker compose up -d
if %errorlevel% neq 0 (
    echo.
    echo  [ERREUR] Docker n'est pas lance ou docker compose n'est pas disponible.
    echo  Installe Docker Desktop : https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo.
echo  Tous les services sont lances.
echo.
echo  Pour arreter : docker compose down
echo  Pour tout supprimer (donnees incluses) : docker compose down -v
echo.
pause

@echo off
title Freenote Frontend
cd /d "%~dp0..\frontend"
echo Installing dependencies...
call npm install
echo.
echo Starting dev server on http://localhost:3000
call npm run dev
pause

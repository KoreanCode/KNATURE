@echo off
chcp 65001 >nul
rem KNATURE 전체 서버 실행 (BO/FO API + 프론트 2종, 창 4개)
cd /d "%~dp0"

echo [1/4] BO API (:9090) 시작...
start "KNATURE BO API :9090" cmd /k "gradlew.bat :knature-bo:bootRun"

echo [2/4] FO API (:8080) 시작...
start "KNATURE FO API :8080" cmd /k "gradlew.bat :knature-fo:bootRun"

echo [3/4] BO 프론트 (:5173) 시작...
start "KNATURE BO FRONT :5173" cmd /k "cd knature-bo-frontend && npm run dev"

echo [4/4] FO 프론트 (:5174) 시작...
start "KNATURE FO FRONT :5174" cmd /k "cd knature-fo-frontend && npm run dev"

echo.
echo 실행 완료 (API는 기동에 30초~1분 소요):
echo   - BO 관리자:  http://localhost:5173  (admin / admin1004)
echo   - FO 쇼핑몰:  http://localhost:5174
pause

@echo off
rem KNATURE - start all servers (BO/FO API + 2 frontends, 4 windows)
cd /d "%~dp0"

echo [1/4] Starting BO API (:9090)...
start "KNATURE BO API :9090" cmd /k "gradlew.bat :knature-bo:bootRun"

echo [2/4] Starting FO API (:8080)...
start "KNATURE FO API :8080" cmd /k "gradlew.bat :knature-fo:bootRun"

echo [3/4] Starting BO front (:5173)...
start "KNATURE BO FRONT :5173" cmd /k "cd knature-bo-frontend && npm run dev"

echo [4/4] Starting FO front (:5174)...
start "KNATURE FO FRONT :5174" cmd /k "cd knature-fo-frontend && npm run dev"

echo.
echo Done. APIs take 30s-1min to boot.
echo   - BO admin : http://localhost:5173  (admin / admin1004)
echo   - FO shop  : http://localhost:5174
pause

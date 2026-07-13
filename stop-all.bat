@echo off
rem KNATURE - stop all servers (kill processes on ports 9090/8080/5173/5174)
powershell -NoProfile -Command "foreach($p in 9090,8080,5173,5174){ $c=Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue; if($c){ $c.OwningProcess | Select-Object -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }; Write-Host ('{0} stopped' -f $p) } else { Write-Host ('{0} not running' -f $p) } }"
pause

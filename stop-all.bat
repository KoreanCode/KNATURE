@echo off
chcp 65001 >nul
rem KNATURE 전체 서버 종료 (포트 9090/8080/5173/5174 점유 프로세스 종료)
powershell -NoProfile -Command "foreach($p in 9090,8080,5173,5174){ $c=Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue; if($c){ $c.OwningProcess | Select-Object -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }; Write-Host ('{0} 종료됨' -f $p) } else { Write-Host ('{0} 이미 꺼짐' -f $p) } }"
pause

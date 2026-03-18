@echo off
echo Iniciando L2Fidas Game Server.

timeout /t 3

cd /d "D:\SERVER_LINE\Workspace\l2fidas\l2fidas - server\game\"
start startGameServer.bat

cd /d "D:\SERVER_LINE\Workspace\l2fidas\l2fidas - server\login\"
start startLoginServer.bat

cd /d "D:\SERVER_LINE\Lineage II-Client_High_Five\system\"
start l2.exe
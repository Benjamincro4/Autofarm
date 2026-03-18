@echo off
title L2Fidas: Game Server Console
color 02
:start
echo Iniciando L2Fidas Game Server.
echo.

java -server -Xmx2048m -Xms2048m -Xmn512m -XX:PermSize=256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts -cp ./../libs/*;L2Fidas.jar fidas.server.gameserver.GameServer

REM NOTE: If you have a powerful machine, you could modify/add some extra parameters for performance, like:
REM -Xms1536m
REM -Xmx3072m
REM -XX:+AggressiveOpts
REM Use this parameters carefully, some of them could cause abnormal behavior, deadlocks, etc.
REM More info here: http://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end

:restart
echo.
echo Admin Restarted Game Server.
echo.
goto start

:error
echo.
echo Game Server Terminated Abnormally!
echo.

:end
echo.
echo Game Server Terminated.
echo.
pause
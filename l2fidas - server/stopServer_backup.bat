set PORT_LOGIN=2106
:: Buscar el PID del proceso que está utilizando el puerto 2106
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%PORT_LOGIN%') do (
    set PID_LOGIN=%%a
)
:: Si se encontró un PID, matar el proceso
if defined PID_LOGIN (
    taskkill /f /pid %PID_LOGIN%
)

set PORT_GAME=7777
:: Buscar el PID del proceso que está utilizando el puerto 7777
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%PORT_GAME%') do (
    set PID_GAME=%%a
)
:: Si se encontró un PID, matar el proceso
if defined PID_GAME (
    taskkill /f /pid %PID_GAME%
)

:: Cerrar l2.exe
taskkill /f /im l2.exe

:: Cerrar todas las consolas cmd.exe activas
for /f "tokens=2 delims=," %%a in ('tasklist /fi "imagename eq cmd.exe" /nh /fo csv') do (
    taskkill /f /pid %%a
)

:: Cerrar todas las consolas cmd.exe activas
for /f "tokens=2 delims=," %%a in ('tasklist /fi "imagename eq conhost.exe" /nh /fo csv') do (
    taskkill /f /pid %%a
)

timeout /t 3
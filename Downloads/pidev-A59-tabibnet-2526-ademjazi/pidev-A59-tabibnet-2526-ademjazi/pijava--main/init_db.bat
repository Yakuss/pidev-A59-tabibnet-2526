@echo off
echo ========================================
echo   TabibNet - Initialisation de la BD
echo ========================================
echo.

REM Configuration MySQL
set MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe
set DB_USER=root
set DB_PASS=

echo Verification de MySQL...
if not exist "%MYSQL_PATH%" (
    echo [ERREUR] MySQL non trouve a %MYSQL_PATH%
    echo.
    echo Veuillez modifier le chemin dans ce script ou installer XAMPP.
    echo Chemins possibles:
    echo   - C:\xampp\mysql\bin\mysql.exe
    echo   - C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
    pause
    exit /b 1
)

echo MySQL trouve!
echo.
echo Initialisation de la base de donnees...
echo.

"%MYSQL_PATH%" -u %DB_USER% < init_database.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCES] Base de donnees initialisee avec succes!
    echo.
    echo Vous pouvez maintenant lancer l'application avec run.bat
) else (
    echo.
    echo [ERREUR] Echec de l'initialisation de la base de donnees!
    echo.
    echo Verifiez que:
    echo   1. MySQL est demarre dans XAMPP
    echo   2. L'utilisateur root existe
    echo   3. Le mot de passe est correct (vide par defaut)
)

echo.
pause

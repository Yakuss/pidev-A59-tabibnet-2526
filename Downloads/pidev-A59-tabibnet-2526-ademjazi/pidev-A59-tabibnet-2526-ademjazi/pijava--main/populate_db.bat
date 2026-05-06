@echo off
echo ========================================
echo   Remplissage de la base de donnees
echo ========================================
echo.

REM Configuration MySQL
set MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe
set DB_USER=root

echo Remplissage de la base de donnees avec des donnees de test...
echo.

"%MYSQL_PATH%" -u %DB_USER% < populate_database.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCES] Base de donnees remplie avec succes!
    echo.
    echo ========================================
    echo   DONNEES AJOUTEES
    echo ========================================
    echo.
    echo   Medecins        : 6 entrees
    echo   Patients        : 6 entrees
    echo   Specialites     : 10 entrees
    echo   Rendez-vous     : 5 entrees
    echo   Questions       : 5 entrees
    echo   Reponses        : 5 entrees
    echo   Magazines       : 5 entrees
    echo   Articles        : 5 entrees
    echo   Evaluations     : 5 entrees
    echo.
    echo ========================================
) else (
    echo.
    echo [ERREUR] Echec du remplissage!
    echo.
    echo Verifiez que:
    echo   1. MySQL est demarre dans XAMPP
    echo   2. La base de donnees 'tabibnet' existe
)

echo.
pause

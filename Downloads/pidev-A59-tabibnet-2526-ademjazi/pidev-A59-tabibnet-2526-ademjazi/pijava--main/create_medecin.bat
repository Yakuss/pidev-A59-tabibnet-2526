@echo off
echo ========================================
echo   Creation du compte Medecin
echo ========================================
echo.

REM Configuration MySQL
set MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe
set DB_USER=root
set DB_PASS=

echo Creation du compte medecin...
echo.

"%MYSQL_PATH%" -u %DB_USER% < create_medecin_account.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCES] Compte medecin cree avec succes!
    echo.
    echo ========================================
    echo   INFORMATIONS DE CONNEXION MEDECIN
    echo ========================================
    echo.
    echo   Email       : medecin@test.com
    echo   Mot de passe: medecin123
    echo.
    echo   Nom         : Dr. Ahmed Ben Ali
    echo   Specialite  : Cardiologie
    echo   Gouvernorat : Tunis
    echo.
    echo ========================================
) else (
    echo.
    echo [ERREUR] Echec de la creation du compte!
    echo.
    echo Verifiez que:
    echo   1. MySQL est demarre dans XAMPP
    echo   2. La base de donnees 'tabibnet' existe
)

echo.
pause

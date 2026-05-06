@echo off
echo ========================================
echo   Creation du compte Patient
echo ========================================
echo.

REM Configuration MySQL
set MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe
set DB_USER=root
set DB_PASS=

echo Creation du compte patient...
echo.

"%MYSQL_PATH%" -u %DB_USER% < create_patient_account.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCES] Compte patient cree avec succes!
    echo.
    echo ========================================
    echo   INFORMATIONS DE CONNEXION PATIENT
    echo ========================================
    echo.
    echo   Email       : patient@test.com
    echo   Mot de passe: patient123
    echo.
    echo   Nom         : Fatma Trabelsi
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

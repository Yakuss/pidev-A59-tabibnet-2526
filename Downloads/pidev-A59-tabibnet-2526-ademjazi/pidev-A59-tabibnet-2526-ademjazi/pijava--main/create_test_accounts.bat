@echo off
echo ========================================
echo   Creation de tous les comptes de test
echo ========================================
echo.

REM Configuration MySQL
set MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe
set DB_USER=root
set DB_PASS=

echo Creation des comptes...
echo.

"%MYSQL_PATH%" -u %DB_USER% < create_all_test_accounts.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCES] Tous les comptes ont ete crees!
    echo.
    echo ========================================
    echo   COMPTES DE TEST DISPONIBLES
    echo ========================================
    echo.
    echo --- ADMINISTRATEUR ---
    echo   Email       : admin@gmail.com
    echo   Mot de passe: admin123
    echo.
    echo --- MEDECIN ---
    echo   Email       : medecin@test.com
    echo   Mot de passe: medecin123
    echo   Nom         : Dr. Ahmed Ben Ali
    echo   Specialite  : Cardiologie
    echo.
    echo --- PATIENT ---
    echo   Email       : patient@test.com
    echo   Mot de passe: patient123
    echo   Nom         : Fatma Trabelsi
    echo.
    echo ========================================
) else (
    echo.
    echo [ERREUR] Echec de la creation des comptes!
    echo.
    echo Verifiez que:
    echo   1. MySQL est demarre dans XAMPP
    echo   2. La base de donnees 'tabibnet' existe
)

echo.
pause

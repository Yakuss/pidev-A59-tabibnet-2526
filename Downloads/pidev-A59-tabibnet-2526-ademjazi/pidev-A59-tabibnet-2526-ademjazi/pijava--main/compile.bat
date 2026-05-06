@echo off
echo ========================================
echo   TabibNet - Compilation du projet
echo ========================================
echo.

REM Chemin vers Maven
set MAVEN_PATH=c:\Users\user\OneDrive\Desktop\TP_3A\pidev-A59-tabibnet-2526-updateferiel\apache-maven-3.9.6\bin\mvn.cmd

echo Compilation en cours...
call "%MAVEN_PATH%" clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCES] Compilation reussie!
) else (
    echo.
    echo [ERREUR] La compilation a echoue!
)

pause

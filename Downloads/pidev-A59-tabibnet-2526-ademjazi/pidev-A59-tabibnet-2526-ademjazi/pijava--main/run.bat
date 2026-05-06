@echo off
echo ========================================
echo   TabibNet - Demarrage de l'application
echo ========================================
echo.

REM Chemin vers Maven
set MAVEN_PATH=c:\Users\user\OneDrive\Desktop\TP_3A\pidev-A59-tabibnet-2526-updateferiel\apache-maven-3.9.6\bin\mvn.cmd

echo Lancement de l'application...
echo.
call "%MAVEN_PATH%" javafx:run

pause

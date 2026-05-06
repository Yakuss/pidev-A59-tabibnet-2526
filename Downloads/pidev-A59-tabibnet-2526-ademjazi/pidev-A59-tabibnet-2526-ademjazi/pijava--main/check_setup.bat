@echo off
echo ========================================
echo   TabibNet - Verification de la Configuration
echo ========================================
echo.

set ERROR_COUNT=0

REM Verification Java
echo [1/4] Verification de Java...
java -version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo   [OK] Java est installe
    java -version 2>&1 | findstr /C:"version"
) else (
    echo   [ERREUR] Java n'est pas installe ou pas dans le PATH
    set /a ERROR_COUNT+=1
)
echo.

REM Verification Maven
echo [2/4] Verification de Maven...
set MAVEN_PATH=c:\Users\user\OneDrive\Desktop\TP_3A\pidev-A59-tabibnet-2526-updateferiel\apache-maven-3.9.6\bin\mvn.cmd
if exist "%MAVEN_PATH%" (
    echo   [OK] Maven trouve
    echo   Chemin: %MAVEN_PATH%
) else (
    echo   [ERREUR] Maven non trouve
    set /a ERROR_COUNT+=1
)
echo.

REM Verification MySQL
echo [3/4] Verification de MySQL...
set MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe
if exist "%MYSQL_PATH%" (
    echo   [OK] MySQL trouve
    echo   Chemin: %MYSQL_PATH%
    
    REM Test de connexion
    echo   Test de connexion...
    "%MYSQL_PATH%" -u root -e "SELECT 'Connexion reussie!' AS Status;" 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo   [OK] Connexion MySQL reussie
    ) else (
        echo   [AVERTISSEMENT] MySQL trouve mais connexion echouee
        echo   Assurez-vous que MySQL est demarre dans XAMPP
    )
) else (
    echo   [ERREUR] MySQL non trouve
    echo   Veuillez installer XAMPP ou modifier le chemin dans ce script
    set /a ERROR_COUNT+=1
)
echo.

REM Verification de la base de donnees
echo [4/4] Verification de la base de donnees...
if exist "%MYSQL_PATH%" (
    "%MYSQL_PATH%" -u root -e "USE tabibnet; SELECT 'Base de donnees OK!' AS Status;" 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo   [OK] Base de donnees 'tabibnet' existe
        
        REM Compter les tables
        for /f "tokens=*" %%a in ('"%MYSQL_PATH%" -u root -e "USE tabibnet; SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='tabibnet';" -s -N 2^>nul') do set TABLE_COUNT=%%a
        echo   Tables trouvees: %TABLE_COUNT%
    ) else (
        echo   [AVERTISSEMENT] Base de donnees 'tabibnet' n'existe pas
        echo   Executez init_db.bat pour creer la base de donnees
    )
) else (
    echo   [IGNORE] MySQL non disponible, verification ignoree
)
echo.

REM Verification des fichiers du projet
echo ========================================
echo   Verification des fichiers du projet
echo ========================================
echo.

set FILES_OK=0
set FILES_TOTAL=0

REM pom.xml
set /a FILES_TOTAL+=1
if exist "pom.xml" (
    echo [OK] pom.xml
    set /a FILES_OK+=1
) else (
    echo [ERREUR] pom.xml manquant
)

REM MainApp.java
set /a FILES_TOTAL+=1
if exist "src\main\java\com\pidev\MainApp.java" (
    echo [OK] MainApp.java
    set /a FILES_OK+=1
) else (
    echo [ERREUR] MainApp.java manquant
)

REM module-info.java
set /a FILES_TOTAL+=1
if exist "src\main\java\module-info.java" (
    echo [OK] module-info.java
    set /a FILES_OK+=1
) else (
    echo [ERREUR] module-info.java manquant
)

REM Scripts
set /a FILES_TOTAL+=1
if exist "run.bat" (
    echo [OK] run.bat
    set /a FILES_OK+=1
) else (
    echo [ERREUR] run.bat manquant
)

set /a FILES_TOTAL+=1
if exist "init_db.bat" (
    echo [OK] init_db.bat
    set /a FILES_OK+=1
) else (
    echo [ERREUR] init_db.bat manquant
)

set /a FILES_TOTAL+=1
if exist "init_database.sql" (
    echo [OK] init_database.sql
    set /a FILES_OK+=1
) else (
    echo [ERREUR] init_database.sql manquant
)

echo.
echo ========================================
echo   RESUME
echo ========================================
echo.

if %ERROR_COUNT% EQU 0 (
    echo [SUCCES] Configuration complete!
    echo.
    echo Fichiers du projet: %FILES_OK%/%FILES_TOTAL%
    echo.
    echo Prochaines etapes:
    echo   1. Demarrez MySQL dans XAMPP
    echo   2. Executez init_db.bat (si pas encore fait)
    echo   3. Executez run.bat pour lancer l'application
) else (
    echo [ATTENTION] %ERROR_COUNT% probleme(s) detecte(s)
    echo.
    echo Fichiers du projet: %FILES_OK%/%FILES_TOTAL%
    echo.
    echo Veuillez corriger les erreurs ci-dessus avant de continuer.
)

echo.
echo ========================================
pause

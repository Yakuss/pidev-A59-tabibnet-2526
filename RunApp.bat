@echo off
set JAVAFX_PATH=C:\Users\user\.m2\repository\org\openjfx
set CP=target\classes;%JAVAFX_PATH%\javafx-base\22.0.1\javafx-base-22.0.1-win.jar;%JAVAFX_PATH%\javafx-graphics\22.0.1\javafx-graphics-22.0.1-win.jar;%JAVAFX_PATH%\javafx-controls\22.0.1\javafx-controls-22.0.1-win.jar;%JAVAFX_PATH%\javafx-fxml\22.0.1\javafx-fxml-22.0.1-win.jar;C:\Users\user\.m2\repository\mysql\mysql-connector-java\8.0.30\mysql-connector-java-8.0.30.jar;C:\Users\user\.m2\repository\com\github\librepdf\openpdf\1.3.30\openpdf-1.3.30.jar

echo Compilation...
"C:\Users\user\.jdks\jbr-17.0.14\bin\javac.exe" -encoding UTF-8 -d target\classes -classpath "%CP%" src\main\java\edu\connexion3a77\entities\*.java src\main\java\edu\connexion3a77\services\*.java src\main\java\edu\connexion3a77\controller\*.java src\main\java\edu\connexion3a77\tests\*.java

echo Lancement...
"C:\Users\user\.jdks\jbr-17.0.14\bin\java.exe" --module-path "%JAVAFX_PATH%\javafx-base\22.0.1\javafx-base-22.0.1-win.jar;%JAVAFX_PATH%\javafx-graphics\22.0.1\javafx-graphics-22.0.1-win.jar;%JAVAFX_PATH%\javafx-controls\22.0.1\javafx-controls-22.0.1-win.jar;%JAVAFX_PATH%\javafx-fxml\22.0.1\javafx-fxml-22.0.1-win.jar" --add-modules javafx.controls,javafx.fxml -classpath "%CP%" edu.connexion3a77.tests.Launcher

pause

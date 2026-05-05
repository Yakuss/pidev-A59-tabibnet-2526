@echo off
echo Installation des dependances...
pip install -r requirements.txt
echo.
echo Demarrage du serveur IA sur http://localhost:8001
echo Appuyez sur Ctrl+C pour arreter.
echo.
python -m uvicorn main:app --host 0.0.0.0 --port 8001 --reload
pause

@echo off
REM Duplo-clique para iniciar o backend a partir do .jar ja compilado
REM (rode compilar-jar.bat pelo menos uma vez antes de usar este arquivo).
REM Deixe esta janela ABERTA enquanto joga.

cd /d "%~dp0"
if not exist "target\pokemon-duel-backend-1.0.0.jar" (
    echo O arquivo .jar ainda nao existe.
    echo Rode compilar-jar.bat primeiro.
    pause
    exit /b 1
)

echo ============================================
echo   Iniciando Pokemon Duel Backend (modo rapido)...
echo   (feche esta janela para desligar o servidor)
echo ============================================
java -jar target\pokemon-duel-backend-1.0.0.jar
pause

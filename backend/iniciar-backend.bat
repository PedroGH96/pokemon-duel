@echo off
REM Duplo-clique neste arquivo para iniciar o backend do Pokemon Duel.
REM Deixe esta janela ABERTA enquanto joga -- fechar ela derruba o servidor.
REM Quando aparecer "Started PokemonDuelApplication", pode abrir o Godot.

cd /d "%~dp0"
echo ============================================
echo   Iniciando Pokemon Duel Backend...
echo   (feche esta janela para desligar o servidor)
echo ============================================
call mvn spring-boot:run
pause

@echo off
REM Rode este arquivo UMA VEZ (ou sempre que mudar o codigo do backend) para
REM gerar um .jar executavel em target\pokemon-duel-backend-1.0.0.jar
REM Depois disso, use iniciar-backend-rapido.bat para subir o servidor sem
REM precisar do Maven recompilar tudo de novo -- inicia bem mais rapido.

cd /d "%~dp0"
echo Compilando o backend em um .jar executavel...
call mvn clean package -DskipTests
echo.
echo Pronto! Use iniciar-backend-rapido.bat para rodar a partir de agora.
pause

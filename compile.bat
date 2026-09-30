@echo off
echo Compiling Real-Time Chess Engine...
if not exist out mkdir out
dir /s /B src\*.java > sources.txt
javac -d out @sources.txt
del sources.txt
echo Compilation complete.

echo.
echo === Running Chess Engine ===
java -cp out Main
pause

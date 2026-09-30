#!/bin/bash
echo "Compiling Real-Time Chess Engine..."
mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt
rm sources.txt
echo "Compilation complete."

echo ""
echo "=== Running Chess Engine ==="
java -cp out Main

@echo off
title Buffer Pool Management System – Cache Inspector
echo ===================================================
echo Starting Buffer Pool Management System - Cache Inspector
echo ===================================================
java -jar "%~dp0target\buffer-pool-cache-inspector-1.0.0.jar"
if %errorlevel% neq 0 (
    echo Rebuilding with Maven...
    mvn compile exec:java
)

@echo off
title Sakay Cebu - Vehicle Monitoring System
color 0A
echo ========================================
echo    SAKAY CEBU
echo    Vehicle Monitoring System
echo ========================================
echo.
echo Starting application...
echo.

set JAVAFX_PATH=C:\Users\there\Downloads\openjfx-26.0.1_windows-x64_bin-sdk\javafx-sdk-26.0.1\lib

java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.web,javafx.fxml -cp . OOP2ProjectFinal.MainFX

pause

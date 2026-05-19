@echo off
cd /d "%~dp0"
echo ========================================
echo    SAKAY CEBU
echo    Vehicle Monitoring System
echo ========================================
echo.
echo Starting application...
echo.

java --module-path "lib" --add-modules javafx.controls,javafx.web,javafx.fxml -cp . OOP2ProjectFinal.MainFX

pause

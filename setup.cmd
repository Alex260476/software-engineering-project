@echo off
rem One-time setup for Windows. Runs scripts\setup.ps1 without changing the PowerShell execution policy.
rem Usage (from the project folder):  .\setup.cmd        or   .\setup.cmd --yes
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\setup.ps1" %*
exit /b %ERRORLEVEL%

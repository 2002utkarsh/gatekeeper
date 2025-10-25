@echo off
if "%1"=="" (
  echo Usage: run.bat ^<url^> [--check-cors] [--check-actuator] [--check-sql]
  exit /b 2
)
java -jar target\gatekeeper-1.0.jar --url %*
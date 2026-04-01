@echo off
cd /d "%~dp0"
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
java -jar target\leihbar-1.0.0.jar
pause

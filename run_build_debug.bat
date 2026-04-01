@echo off
cd /d "C:\Users\z004mstn\OneDrive - Siemens Energy\DH\Software Enigneering\Zweitversuch\leihbar"
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Using Java:
java -version
echo.
echo Building with verbose output...
mvn clean package -e -X

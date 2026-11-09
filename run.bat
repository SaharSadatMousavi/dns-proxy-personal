@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Compiling Java files...
javac -encoding UTF-8 -d bin src\*.java

if %ERRORLEVEL% equ 0 (
    echo Running DnsProxyApp...
    java -cp bin DnsProxyApp
) else (
    echo Compilation failed!
    pause
)

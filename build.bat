@echo off
REM Compile the project from the command line and bundle it into a JAR (Windows).
REM Usage:  build.bat     (then run:  java -jar library-management.jar)

if exist out rmdir /s /q out
mkdir out

echo 1) Compiling (javac turns .java files into bytecode .class files)...
dir /s /b src\main\java\*.java > sources.txt
javac -d out @sources.txt
if errorlevel 1 goto error
del sources.txt

echo 2) Creating the JAR (Main-Class = library.Main)...
jar cfe library-management.jar library.Main -C out .
if errorlevel 1 goto error

echo Done. Run it with:  java -jar library-management.jar
echo Threads demo only:  java -jar library-management.jar --demo
goto end

:error
echo Build failed.

:end

#!/bin/bash
# Compile the project from the command line and bundle it into a JAR.
# Usage:  ./build.sh        (then run:  java -jar library-management.jar)

set -e

rm -rf out
mkdir out

echo "1) Compiling (javac turns .java files into bytecode .class files)..."
javac -d out $(find src/main -name "*.java")

echo "2) Creating the JAR (Main-Class = library.Main)..."
jar cfe library-management.jar library.Main -C out .

echo "Done. Run it with:  java -jar library-management.jar"
echo "Threads demo only:  java -jar library-management.jar --demo"

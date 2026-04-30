@echo off
echo Compiling Java files...
mkdir -p target\classes

javac -d target\classes src\main\java\com\example\*.java src\main\java\com\example\*.java src\main\java\com\example\model\*.java src\main\java\com\example\parser\*.java src\main\java\com\example\analyzer\*.java src\main\java\com\example\utils\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
pause
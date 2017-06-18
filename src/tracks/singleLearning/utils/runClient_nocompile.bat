@echo off

set agent=%1
set port=%2
set clientType=%3

set path=%PATH%;C:\Program Files\Java\jdk1.8.0_121\bin

set src_folder=clients\GVGAI-JavaClient\src
set build_folder=clients\GVGAI-JavaClient\out

set py_src_folder=clients\GVGAI-PythonClient\src

set gson=lib\gson-2.8.0.jar


if "%clientType%" == "java" (
    if not exist %build_folder% mkdir %build_folder%
    dir /s/b %src_folder%\*.java > sources.txt
    javac -cp %gson% -d %build_folder% @sources.txt

    rem Run the JavaClient class
    java -agentlib:jdwp=transport=dt_socket,server=y,address=%port%,suspend=n -cp %build_folder%;%gson% utils.JavaClient %agent%  > logs/output_client_redirect.txt 2> logs/output_client_redirect_err.txt
) else if "%clientType%" == "python" (
    rem Run the PythonClient class
    python %py_src_folder%\PythonClient.py %agent%  > logs/output_client_redirect_python.txt 2> logs/output_client_redirect_err_python.txt
)

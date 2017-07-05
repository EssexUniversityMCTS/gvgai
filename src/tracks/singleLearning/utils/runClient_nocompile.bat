@echo off

set agent=%1
set port=%2
set clientType=%3
set srcPrefix=%4

set path=%PATH%;C:\Program Files\Java\jdk1.8.0_121\bin

set src_folder=%srcPrefix%\clients\GVGAI-JavaClient\src
set py_src_folder=%srcPrefix%\clients\GVGAI-PythonClient\src\utils
set build_folder=%srcPrefix%\clients\GVGAI-JavaClient\client-out

if "%clientType%" == "java" (
    if not exist %build_folder% mkdir %build_folder%
    dir /s/b %src_folder%\*.java > sources.txt
    javac -d %build_folder% @sources.txt

    rem Run the JavaClient class
    java -cp %build_folder% utils.JavaClient -agentName %agent%  > logs/output_client_redirect.txt 2> logs/output_client_redirect_err.txt
	) 

if "%clientType%" == "python" (
    rem Run the PythonClient class
    c:\python35\python.exe %py_src_folder%\PythonClient.py %agent%  > logs/output_client_redirect_python.txt 2> logs/output_client_redirect_err_python.txt
	)
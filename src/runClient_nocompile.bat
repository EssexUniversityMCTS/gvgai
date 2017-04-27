@echo off

set path=%PATH%;C:\Program Files\Java\jdk1.8.0_121\bin
set build_folder=build
set gson=gson-2.6.2.jar
set src=..\clients\GVGAI-JavaClient\src
set root_path=..\

rem This script presumes that all the client-related Java files have been previously compiled and put in a folder called "build"

rem Run the JavaClient class	
java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n -cp src\%build_folder%;%gson% JavaClient
@echo off

set agent=%1
set port=%2

set path=%PATH%;C:\Program Files\Java\jdk1.8.0_121\bin
set build_folder=..\..\..\client\GVGAI-JavaClient\out
set src=..\..\..\clients\GVGAI-JavaClient\src
set root_path=..\..\..\

rem This script presumes that all the client-related Java files have been previously compiled and put in a folder called "build"

rem Run the JavaClient class	
java -agentlib:jdwp=transport=dt_socket,server=y,address=%port%,suspend=n -cp %build_folder%;%gson% JavaClient %agent%
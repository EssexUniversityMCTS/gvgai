@echo off

set SERVER_GAMES_DIR=../../

set path=%PATH%;C:\Program Files\Java\jdk1.8.0_121\bin

set src_folder=..\..\src
set build_folder=server-out


if not exist %build_folder% mkdir %build_folder%
dir /s/b %src_folder%\*.java > sources.txt
javac -d %build_folder% @sources.txt

rem Run the JavaClient class
java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n -cp %build_folder% tracks.singleLearning.utils.JavaServer %SERVER_GAMES_DIR% > logs/output_server_redirect.txt 2> logs/output_server_redirect_err.txt

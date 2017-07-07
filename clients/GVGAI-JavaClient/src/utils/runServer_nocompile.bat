@echo off

set path=%PATH%;C:\Program Files\Java\jdk1.8.0_121\bin

set game_id=%1
set server_dir_prefix=%2
set games_prefix=%2
set visuals=%3

set src_folder=%server_dir_prefix%\src
set build_folder=server-out

if not exist %build_folder% mkdir %build_folder%
dir /s/b %src_folder%\*.java > sources.txt
javac -d %build_folder% @sources.txt


if "%visuals%"=="true" (
     java -agentlib:jdwp=transport=dt_socket,server=y,address=8888,suspend=n -cp %build_folder% tracks.singleLearning.utils.JavaServer -gameId %game_id% -gamesDir %games_prefix% -visuals > logs/output_server_redirect.txt 2> logs/output_server_redirect_err.txt
 ) else (
     java -agentlib:jdwp=transport=dt_socket,server=y,address=8888,suspend=n -cp %build_folder% tracks.singleLearning.utils.JavaServer -gameId %game_id% -gamesDir %games_prefix% > logs/output_server_redirect.txt 2> logs/output_server_redirect_err.txt
 )
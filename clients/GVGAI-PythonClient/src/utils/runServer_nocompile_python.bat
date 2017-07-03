@echo off
set gameId=0

set gamesDir=..\..\..\
set path=%PATH%;C:\Program Files\Java\jdk1.8.0_121\bin

set src_folder=..\..\..\src
set build_folder=out

if not exist %build_folder% mkdir %build_folder%
dir /s/b %src_folder%\*.java > sources.txt
javac -d %build_folder% @sources.txt

rem Run the JavaClient class
java -cp %build_folder% tracks.singleLearning.utils.JavaServer -gameId %gameId% -gamesDir %gamesDir% > logs/output_server_redirect.txt 2> logs/output_server_redirect_err.txt

cmd /k
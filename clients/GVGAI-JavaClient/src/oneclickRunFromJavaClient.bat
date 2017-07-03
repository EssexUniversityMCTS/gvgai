@echo off

set gameId=0
set shDir=utils
set serverDir=../../..

set DIRECTORY=./logs
if not exist %DIRECTORY% mkdir %DIRECTORY%


rem Build the client
set src_folder=../../
set build_folder=client-out


if not exist %build_folder% mkdir %build_folder%

dir /s/b %src_folder%\*.java > sources.txt
javac -d %build_folder% @sources.txt
rem run with screen visualisation
java -cp %build_folder% TestLearningClient -gameId %gameId% -shDir %shDir% -serverDir %serverDir% -visuals
rem run without screen visualisation
rem java -cp %build_folder% TestLearningClient -gameId %gameId% -shDir %shDir% -serverDir %serverDir%
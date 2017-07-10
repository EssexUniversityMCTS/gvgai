@echo off

set gameId=0
set shDir=utils
set serverDir=..\..\..

set DIRECTORY=logs
if not exist %DIRECTORY% mkdir %DIRECTORY%

rem Run the PythonClient class with visualisation off
rem python TestLearningClient.py -shDir %shDir% -serverDir %serverDir%

rem Run the PythonClient class with visualisation on
C:\Python27\python TestLearningClient.py -shDir %shDir% -serverDir %serverDir% -visuals

cmd /k
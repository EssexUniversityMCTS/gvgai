@echo off

set gameId=0
set shDir=utils
set serverDir=..\..\..

set DIRECTORY=logs
if not exist %DIRECTORY% mkdir %DIRECTORY%

rem Run the PythonClient class with visualisation off
rem C:\Python35\python.exe TestLearningClient.py -shDir %shDir% -serverDir %serverDir%

rem Run the PythonClient class with visualisation on
C:\Python35\python.exe TestLearningClient.py -shDir %shDir% -serverDir %serverDir% -visuals
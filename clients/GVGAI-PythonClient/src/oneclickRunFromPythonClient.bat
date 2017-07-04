@echo off
set gameId=0
set server_dir=..\..\..\
set agent_name=sampleAgents.Agent

rem Run the PythonClient class with visualisation off
rem C:\Python35\python.exe TestLearningClient.py -serverDir %server_dir%

rem Run the PythonClient class with visualisation on
C:\Python35\python.exe TestLearningClient.py -serverDir %server_dir% -visuals

cmd /k

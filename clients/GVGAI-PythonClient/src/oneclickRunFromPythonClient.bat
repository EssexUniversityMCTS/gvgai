@echo off
set gameId=0
set server_dir=..\..\..\
set agent_name=sampleAgents.Agent

rem Run the PythonClient class
rem C:\Python35\python.exe TestLearningClient.py -gameId %gameId% -serverDir %server_dir% -agentName %agent_name%  > logs/output_client_redirect_python.txt 2> logs/output_client_redirect_err_python.txt

rem Run the PythonClient class with visuals
C:\Python35\python.exe TestLearningClient.py -gameId %gameId% -serverDir %server_dir% -agentName %agent_name% -visuals > logs/output_client_redirect_python.txt 2> logs/output_client_redirect_err_python.txt

cmd /k

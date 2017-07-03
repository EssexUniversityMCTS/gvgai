@echo off
set gameId=0
set server_dir=..\..\..\
set agent_name=sampleAgents.Agent

rem Run the PythonClient class
c:\python35\python.exe TestLearningClient.py  -gameId %game_id% -serverDir %server_dir% -agentName %agent_name%  > logs/output_client_redirect_python.txt 2> logs/output_client_redirect_err_python.txt#python TestLearningClient.py -gameId ${game_id} -serverDir ${server_dir} -agentName ${agent_name} -visuals > ${DIRECTORY}/output_client_redirect_python.txt 2> ${DIRECTORY}/output_client_redirect_err_python.txt

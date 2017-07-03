#!/bin/bash

game_id=0
server_dir="../../.."
agent_name="sampleAgents.Agent"



DIRECTORY='./logs'
if [ ! -d "$DIRECTORY" ]; then
  mkdir ${DIRECTORY}
fi

# Build the client
#python TestLearningClient.py -gameId ${game_id} -serverDir ${server_dir} -agentName ${agent_name} -visuals > ${DIRECTORY}/output_client_redirect_python.txt 2> ${DIRECTORY}/output_client_redirect_err_python.txt
python TestLearningClient.py -gameId ${game_id} -serverDir ${server_dir} -agentName ${agent_name}  > ${DIRECTORY}/output_client_redirect_python.txt 2> ${DIRECTORY}/output_client_redirect_err_python.txt

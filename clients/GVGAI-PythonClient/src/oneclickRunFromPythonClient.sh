#!/bin/bash

game_id=0
server_dir="../../.."
agent_name="sampleAgents.Agent"



DIRECTORY='./logs'
if [ ! -d "$DIRECTORY" ]; then
  mkdir ${DIRECTORY}
fi

# Run the client with visualisation on
#python TestLearningClient.py -gameId ${game_id} -serverDir ${server_dir} -agentName ${agent_name} -visuals
# Run the client with visualisation off
python TestLearningClient.py -gameId ${game_id} -serverDir ${server_dir} -agentName ${agent_name}

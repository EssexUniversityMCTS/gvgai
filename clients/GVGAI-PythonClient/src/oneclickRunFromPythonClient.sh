#!/bin/bash

game_id=2
server_dir=../../..
agent_name=sampleAgents.Agent
sh_dir=utils


DIRECTORY='./logs'
if [ ! -d "$DIRECTORY" ]; then
  mkdir ${DIRECTORY}
fi

# Run the client with visualisation on
python TestLearningClient.py -gameId ${game_id} -agentName ${agent_name} -serverDir ${server_dir} -shDir ${sh_dir} -visuals
# Run the client with visualisation off
# python TestLearningClient.py -serverDir ${server_dir} -gamesDir ${server_dir} -gameId ${game_id}

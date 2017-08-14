#!/bin/bash

# Got an java.net.BindException: Address already in use (Bind failed) from the server?
# Maybe a process is running at that port. Check: lsof -i tcp:<port>


gameId=0
shDir='utils'
serverDir='../../..'
DIRECTORY='./logs'

if [ ! -d "$DIRECTORY" ]; then
  mkdir ${DIRECTORY}
fi

# Build the client
src_folder='../../'
build_folder='client-out'

rm -rf ${build_folder}
mkdir -p ${build_folder}
find ${src_folder} -name "*.java" > sources.txt
javac -d ${build_folder} @sources.txt
# run with screen visualisation on
#java -classpath ${build_folder} TestLearningClient -shDir ${shDir} -serverDir ${serverDir} -gameId ${gameId} -visuals
# run without screen visualisation off
java -classpath ${build_folder} TestLearningClient -shDir ${shDir} -serverDir ${serverDir} -gameId ${gameId} -agentName sampleLearner.Agent

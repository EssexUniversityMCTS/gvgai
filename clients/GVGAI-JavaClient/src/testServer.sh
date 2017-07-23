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

build_folder='build'

rm -rf ${build_folder}
mkdir -p ${build_folder}

javac -d build -Xlint:unchecked $(find "."  | grep \\\.java$)
java -classpath build: -Xms512m -Xmx2048m TestLearningClient -gameId ${gameId} -shDir ${shDir} -serverDir ${serverDir} -visuals

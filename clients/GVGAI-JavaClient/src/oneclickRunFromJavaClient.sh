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
shell_dir=`dirname "$(find . -type f -name runServer_nocompile.sh | head -1)"`
javac -d ${build_folder} @sources.txt
java -classpath ${build_folder} TestLearningClient -gameId 0 -shDir ${shDir} -serverDir ${serverDir} -visuals
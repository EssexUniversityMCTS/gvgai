#!/bin/bash
# Got an java.net.BindException: Address already in use (Bind failed) from the server?
# Maybe a process is running at that port. Check: lsof -i tcp:<port>


DIRECTORY='./logs'
if [ ! -d "$DIRECTORY" ]; then
  mkdir ${DIRECTORY}
fi

# Build the server
src_folder='../../'
build_folder='server-out'

rm -rf ${build_folder}
mkdir -p ${build_folder}
find ${src_folder} -name "*.java" > sources.txt
shell_dir=`dirname "$(find . -type f -name runClient_nocompile.sh | head -1)"`
javac -d ${build_folder} @sources.txt
java -classpath ${build_folder} tracks.singleLearning.TestSingleLearning -clientType python -gameId 0 -shDir ${shell_dir} -clientDir ../../.. -gamesDir ../../..
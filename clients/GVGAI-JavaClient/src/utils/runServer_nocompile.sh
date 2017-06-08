#!/bin/bash

# Got an java.net.BindException: Address already in use (Bind failed) from the server?
# Maybe a process is running at that port. Check: lsof -i tcp:<port>

DIRECTORY=logs
if [ ! -d "$DIRECTORY" ]; then
  mkdir $DIRECTORY
fi

#Point at the folder that contains 'examples/'
SERVER_GAMES_DIR='../../'

src_folder='../../src'
build_folder='server-out'

rm -rf $build_folder
mkdir -p $build_folder
find ${src_folder} -name "*.java" > sources.txt
javac -d $build_folder @sources.txt

java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n -classpath ${build_folder} tracks.singleLearning.utils.JavaServer ${SERVER_GAMES_DIR} > logs/output_server_redirect.txt 2> logs/output_server_redirect_err.txt
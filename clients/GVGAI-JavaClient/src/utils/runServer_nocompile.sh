#!/bin/bash

# Got an java.net.BindException: Address already in use (Bind failed) from the server?
# Maybe a process is running at that port. Check: lsof -i tcp:<port>

game_id=$1
server_dir_prefix=$2
games_prefix=$2
visuals=$3

DIRECTORY='./logs'
if [ ! -d "$DIRECTORY" ]; then
  mkdir ${DIRECTORY}
fi

#Point at the folder that contains 'examples/'
server_dir="${server_dir_prefix}/src"
build_folder='server-out'

rm -rf ${build_folder}
mkdir -p ${build_folder}
find ${server_dir} -name "*.java" > sources.txt
javac -d ${build_folder} @sources.txt

if [ ${visuals} = "true" ]; then
    java -agentlib:jdwp=transport=dt_socket,server=y,address=8888,suspend=n -classpath ${build_folder} tracks.singleLearning.utils.JavaServer -gameId ${game_id} -gamesDir ${games_prefix} -visuals > ${DIRECTORY}/output_server_redirect.txt 2> ${DIRECTORY}/output_server_redirect_err.txt
else
    java -agentlib:jdwp=transport=dt_socket,server=y,address=8888,suspend=n -classpath ${build_folder} tracks.singleLearning.utils.JavaServer -gameId ${game_id} -gamesDir ${games_prefix} > ${DIRECTORY}/output_server_redirect.txt 2> ${DIRECTORY}/output_server_redirect_err.txt
fi
#!/bin/bash

# Got an java.net.BindException: Address already in use (Bind failed) from the server?
# Maybe a process is running at that port. Check: lsof -i tcp:<port>

server_jar=$1
game_id=$2
games_prefix=$3
game_file=$4
level_file=$5
log_dir="logs"


#java -cp ${server_jar} -agentlib:jdwp=transport=dt_socket,server=y,address=8888,suspend=n tracks.singleLearning.utils.JavaServer -gameId ${game_id} -gamesDir ${games_prefix} > ${log_dir}/output_server_redirect_game${game_id}.txt 2> ${log_dir}/output_server_redirect_err_game${game_id}.txt
java -cp ${server_jar} -agentlib:jdwp=transport=dt_socket,server=y,address=8888,suspend=n core.competition.LearnerExecutor -gameId ${game_id} -gameFile ${game_file} -levelFile ${level_file} -gamesDir ${games_prefix} > ${log_dir}/output_server_redirect_game${game_id}.txt 2> ${log_dir}/output_server_redirect_err_game${game_id}.txt

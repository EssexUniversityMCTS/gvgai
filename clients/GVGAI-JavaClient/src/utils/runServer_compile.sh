#!/bin/bash

# Got an java.net.BindException: Address already in use (Bind failed) from the server?
# Maybe a process is running at that port. Check: lsof -i tcp:<port>

game_id=$1
games_prefix=$2
visuals=$3
DIRECTORY='./logs'


if [ ${visuals} = "true" ]; then
    java -jar -agentlib:jdwp=transport=dt_socket,server=y,address=8888,suspend=n /home/vgdl/learning1p_competition/gvgai.jar -gameId ${game_id} -gamesDir ${games_prefix} -visuals > ${DIRECTORY}/output_server_redirect_game${game_id}.txt 2> ${DIRECTORY}/output_server_redirect_err_game${game_id}.txt
else
    java -jar -agentlib:jdwp=transport=dt_socket,server=y,address=8888,suspend=n /home/vgdl/learning1p_competition/gvgai.jar -gameId ${game_id} -gamesDir ${games_prefix} > ${DIRECTORY}/output_server_redirect_game${game_id}.txt 2> ${DIRECTORY}/output_server_redirect_err_game${game_id}.txt
fi
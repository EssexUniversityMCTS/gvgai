#!/bin/bash

DIRECTORY=logs
if [ ! -d "$DIRECTORY" ]; then
  mkdir $DIRECTORY
fi

agent=$1
port=$2
src_folder='clients/GVGAI-JavaClient/src'
build_folder='clients/GVGAI-JavaClient/out'

mkdir -p $build_folder
find ${src_folder} -name "*.java" > sources.txt
javac -d $build_folder @sources.txt

java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n -classpath ${build_folder} utils.JavaClient ${agent} > logs/output_client_redirect.txt 2> logs/output_client_redirect_err.txt
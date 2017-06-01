#!/bin/bash

DIRECTORY=logs
if [ ! -d "$DIRECTORY" ]; then
  mkdir $DIRECTORY
fi

port=$2
src_folder='src'
build_folder='out'
gson='lib/gson-2.8.0.jar'

mkdir -p $build_folder
find ${src_folder} -name "*.java" > sources.txt
javac -cp ${gson} -d $build_folder @sources.txt

java -agentlib:jdwp=transport=dt_socket,server=y,address=${port},suspend=n -classpath ${build_folder}:${gson} tracks.singleLearning.utils.JavaServer > logs/output_server_redirect.txt
#!/bin/bash

DIRECTORY=logs
if [ ! -d "$DIRECTORY" ]; then
  mkdir $DIRECTORY
fi

agent=$1
port=$2
clientType=$3
src_prefix=$4
src_folder="${src_prefix}/clients/GVGAI-JavaClient/src"
build_folder="${src_prefix}/clients/client-out"

mkdir -p $build_folder
find ${src_folder} -name "*.java" > sources.txt
javac -d $build_folder @sources.txt

java -agentlib:jdwp=transport=dt_socket,server=y,address=8888,suspend=n -classpath ${build_folder} utils.JavaClient ${agent}

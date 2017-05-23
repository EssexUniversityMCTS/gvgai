#!/bin/bash

DIRECTORY=logs
if [ ! -d "$DIRECTORY" ]; then
  mkdir $DIRECTORY
fi

build_folder='../../../clients/GVGAI-JavaClient/out'
gson='../../../lib/gson-2.8.0.jar'

java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n -classpath ${build_folder}:${gson} JavaClient

#Uncomment this line instead for debug only (all printed out will go to output_redirect.txt. SERVER-CLIENT WON'T WORK
#java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n -classpath ${build_folder}:${gson} JavaClient > logs/output_redirect.txt
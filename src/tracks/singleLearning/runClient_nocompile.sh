#!/bin/bash

DIRECTORY=logs
if [ ! -d "$DIRECTORY" ]; then
  mkdir $DIRECTORY
fi

build_folder='clients/GVGAI-JavaClient/out/'
gson='gson-2.6.2.jar'
#echo "java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n -classpath ${build_folder}:${gson} JavaClient"
java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n -classpath ${build_folder}:${gson} JavaClient

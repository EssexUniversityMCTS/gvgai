#!/bin/bash

DIRECTORY=logs
if [ ! -d "$DIRECTORY" ]; then
  mkdir $DIRECTORY
fi;

agent=$1
port=$2
clientType=$3
src_folder='clients/GVGAI-JavaClient/src'
py_src_folder='clients/GVGAI-PythonClient/src/utils'
build_folder='clients/GVGAI-JavaClient/out'
gson='lib/gson-2.8.0.jar'


if [ "$clientType" = "java" ]; then
	mkdir -p $build_folder
	find ${src_folder} -name "*.java" > sources.txt
	javac -cp ${gson} -d $build_folder @sources.txt
	java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n -classpath ${build_folder}:${gson} utils.JavaClient ${agent} > logs/output_client_redirect.txt 2> logs/output_client_redirect_err.txt
elif [ "$clientType" = "python" ]; then
        echo ${agent}
	python ${py_src_folder}/PythonClient.py ${agent}  > logs/output_client_redirect_python.txt 2> logs/output_client_redirect_err_python.txt
fi;
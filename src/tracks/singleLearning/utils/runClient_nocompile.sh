#!/bin/bash

pwd
DIRECTORY=logs
if [ ! -d "$DIRECTORY" ]; then
  mkdir ${DIRECTORY}
fi;

agent=$1
port=$2
clientType=$3
src_prefix=$4
src_folder="${src_prefix}/clients/GVGAI-JavaClient/src"
py_src_folder="${src_prefix}/clients/GVGAI-PythonClient/src/utils"
build_folder="${src_prefix}/clients/client-out"

echo "$1 $2 $3 $4"
if [ "$clientType" = "java" ]; then
	mkdir -p ${build_folder}
	find ${src_folder} -name "*.java" > sources.txt
	javac -d ${build_folder} @sources.txt
	java -agentlib:jdwp=transport=dt_socket,server=y,address=8888,suspend=n -classpath ${build_folder} utils.JavaClient ${agent} > logs/output_client_redirect.txt 2> logs/output_client_redirect_err.txt
elif [ "$clientType" = "python" ]; then
        echo ${agent}
	python ${py_src_folder}/PythonClient.py ${agent}  > logs/output_client_redirect_python.txt 2> logs/output_client_redirect_err_python.txt
fi;

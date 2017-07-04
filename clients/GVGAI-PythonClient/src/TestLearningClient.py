import logging
import os
import subprocess
import sys
import traceback
import argparse

sys.path.append('./utils')

from CompetitionParameters import CompetitionParameters

from ClientComm import ClientComm

if __name__ == "__main__":
    gameId = 0
    agentName = 'sampleAgents.Agent'
    serverDir = '..\\..\\..'
    visuals = True
    shDir = "utils"
    gamesDir = "..\\.."
    if len(sys.argv) > 0:
        parser = argparse.ArgumentParser(description='Test python client')
        parser.add_argument('TestLearningClient.py')
        parser.add_argument('-gameId', action="store", dest="gameId", type=int)
        parser.add_argument('-agentName', action="store", dest="agentName")
        parser.add_argument('-serverDir', action="store", dest="serverDir")
        parser.add_argument('-visuals', action="store_true", dest="visuals")
        parser.add_argument('-shDir', action="store", dest="shDir")
        parser.add_argument('-gamesDir', action="store", dest="gamesDir")
        parser.parse_args(sys.argv)

    print("Run game " + str(gameId) + " with agent " + agentName)
    if CompetitionParameters.OS_WIN:
        scriptFile = shDir + "\\runServer_nocompile_python.bat " + str(gameId) + " " + serverDir + " " + str(visuals)
    else:
        scriptFile = os.path.join(shDir, "runServer_nocompile_python.sh " + str(gameId) + " " + serverDir +
                                  " " + str(visuals))
    try:
        p = subprocess.Popen(scriptFile, shell=True)
        print("Run server process [OK]")
        # print(str(os.getcwd()))
        # stdout, stderr = p.communicate()
        print("Run server communicate [OK]")
        ccomm = ClientComm(agentName)
        print("Start comm with agent " + agentName)
        ccomm.startComm()
        print("Server process started [OK]")
    except Exception as e:
        logging.exception(e)
        print("Server process started [FAILED]")
        traceback.print_exc()
        sys.exit()

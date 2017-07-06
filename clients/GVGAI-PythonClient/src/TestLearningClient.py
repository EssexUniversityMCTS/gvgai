import logging
import os
import subprocess
import sys
import traceback
import argparse

sys.path.append("./utils")

from CompetitionParameters import CompetitionParameters

from ClientComm import ClientComm

if __name__ == "__main__":

    # read arguments
    if CompetitionParameters.OS_WIN:
        serverDirDefault = '..\\..\\..'
    else:
        serverDirDefault = '../../..'
    parser = argparse.ArgumentParser(description="TestLearningClient.py")
    parser.add_argument('TestLearningClient.py')
    parser.add_argument('-gameId', action="store", dest='gameId', type=int, default=0)
    parser.add_argument('-agentName', action="store", dest='agentName', default='sampleAgents.Agent')
    parser.add_argument('-serverDir', action="store", dest='serverDir', default=serverDirDefault)
    parser.add_argument('-shDir', action="store", dest='shDir', default='utils')
    parser.add_argument('-visuals', action="store_true", dest='visuals', default=False)
    args = parser.parse_args(sys.argv)
    # set variables
    gameId = args.gameId
    serverDir = args.serverDir
    agentName = args.agentName
    shDir = args.shDir
    visuals = args.visuals
    gamesDir = serverDir

    print("Run game " + str(gameId) + " with agent " + agentName)
    if CompetitionParameters.OS_WIN:
        scriptFile = shDir + "\\runServer_nocompile_python.bat " + str(gameId) + " " + str(serverDir) + " " + str(visuals)
    else:
        scriptFile = os.path.join(shDir, "runServer_nocompile_python.sh " + str(gameId) + " " + str(serverDir) +
                                  " " + str(visuals))
    try:
        print("scriptFile to run is: "+scriptFile)
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

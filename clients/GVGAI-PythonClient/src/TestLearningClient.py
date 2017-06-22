import logging
import os
import subprocess
import sys
import traceback
sys.path.append('./utils')

from CompetitionParameters import CompetitionParameters

from ClientComm import ClientComm

if __name__ == "__main__":
    agentName = 'sampleAgents.Agent'

    if CompetitionParameters.OS_WIN:
        scriptFile = "utils\\runServer_nocompile_python.bat"
    else:
        scriptFile = os.path.join("utils", "runServer_nocompile_python.sh")

    try:
        p = subprocess.Popen(scriptFile)
        print("Run server process [OK]")
        print(str(os.getcwd()))
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

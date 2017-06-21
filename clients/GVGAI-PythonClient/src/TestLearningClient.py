import logging
import os.path
import subprocess
import sys
import os
import traceback
from ClientComm import ClientComm
from CompetitionParameters import CompetitionParameters

if __name__ == "__main__":
    agentName = 'sampleAgents.Agent'

    if CompetitionParameters.OS_WIN:
        scriptFile = "utils\\runServer_nocompile_python.bat"
    else:
        scriptFile = os.path.join("utils", "runServer_nocompile_python.sh")

    try:
        if __name__ == '__main__':
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

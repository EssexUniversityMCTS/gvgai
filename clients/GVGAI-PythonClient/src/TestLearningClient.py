import logging
import os.path
import subprocess
import sys
import traceback
from ClientComm import ClientComm
from CompetitionParameters import CompetitionParameters


class TestLearningClient:
    """
     * This is the class to run client and server
    """

    def __init__(self):
        self.agentName = 'SampleRandomAgent'

        if CompetitionParameters.OS_WIN:
            self.scriptFile = "utils\\runServer_nocompile_python.bat"
        else:
            self.scriptFile = os.path.join("utils", "runServer_nocompile_python.sh")

        try:
            if __name__ == '__main__':
                p = subprocess.Popen(self.scriptFile, stdin=subprocess.PIPE, stdout=subprocess.PIPE)
                # print("Run server process [OK]")
                stdout, stderr = p.communicate()
                print("Run server communicate [OK]")
                ccomm = ClientComm(self.agentName)
                print("Start comm with agent " + self.agentName)
                ccomm.startComm()
                print("Server process started [OK]")
        except Exception as e:
            logging.exception(e)
            print("Server process started [FAILED]")
            traceback.print_exc()
            sys.exit()


lc = TestLearningClient()

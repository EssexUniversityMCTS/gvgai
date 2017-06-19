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

        if CompetitionParameters.OS_WIN:
            self.scriptFile = 'utils\\runServer_nocompile_python.bat'
        else:
            self.scriptFile = os.path.join('utils', 'runServer_nocompile_python.sh')

        try:
            if __name__ == '__main__':
                p = subprocess.Popen(self.scriptFile)
                stdout, stderr = p.communicate()
                print("Server process started [OK]")
        except Exception as e:
            logging.exception(e)
            print("Server process started [FAILED]")
            traceback.print_exc()
            sys.exit()

        self.agentName = 'SampleRandomAgent'
        ccomm = ClientComm(self.agentName)
        ccomm.startComm()


lc = TestLearningClient()

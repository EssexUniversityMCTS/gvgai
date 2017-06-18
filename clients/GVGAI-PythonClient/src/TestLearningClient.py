from ClientComm import ClientComm
from CompetitionParameters import CompetitionParameters
import subprocess, traceback, sys


class TestLearningClient:
    def __init__(self):
        self.scriptFile = ""
        
        if(CompetitionParameters.OS_WIN):
            self.scriptFile = "utils\\runServer_nocompile_python.bat"
        else:
            self.scriptFile = "utils/runServer_nocompile_python.sh"
            
        self.agentName = "SampleRandomAgent"
        
        try:
            if __name__ == '__main__':
                p = subprocess.Popen(self.scriptFile)
                stdout, stderr = p.communicate()
                print("Server process started [OK]")
        except:
            print("Server process started [FAIL]")
            traceback.print_exc()
            sys.exit()  
           
        ccomm = ClientComm(self.agentName)
        ccomm.startComm()

lc = TestLearningClient()

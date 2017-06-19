import sys
import os
from ClientComm import ClientComm

class PythonClient:
     def __init__(self, args):
        if len(args) == 1:
            agentName = args[0]
        ccomm = ClientComm(self.agentName)
        ccomm.startComm()
        



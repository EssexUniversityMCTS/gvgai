import sys, traceback
import os
from CompetitionParameters import CompetitionParameters
from IOSocket import IOSocket
from AbstractPlayer import *
from SerializableStateObservation import *


class ClientComm:
    TOKEN_SEP = '#'
    
    def __init__(self, agentName):
        self.io = IOSocket(CompetitionParameters.SOCKET_PORT)
        self.sso = SerializableStateObservation()
        self.LOG = False
        self.agentName = agentName
        self.lasMessageId = 0

    def startComm(self):
        self.io.initBuffers()

        try:
            self.listen()
        except:
            print ('Failed to listen.')
            traceback.print_exc()

    """
     * Method that perpetually listens for messages from the server.
     * With the use of additional helper methods, this function interprets
     * messages and represents the core response-generation methodology of the agent.
     * @throws IOException
    """
    def listen(self):
        line = ''

        while (line is not None):
            line = self.io.readLine()

            self.processLine(line)

            if (self.sso.phase == SerializableStateObservation.Phase['START']):
                self.start()

            if (self.sso.phase == SerializableStateObservation.Phase['INIT']):
                self.init()

            elif (self.sso.phase == SerializableStateObservation.Phase['ACT']):
                self.result()

            else:
                self.io.writeToServer(lastMessageId, 'null', LOG)

    """
    Helper method that converts a given dictionary into
    a correct SSO type
    """
    def as_sso(self, d):
        self.sso.__dict__.update(d)
        return self.sso

    """
     * Method that interprets the received messages from the server's side.
     * A message can either be a string (in the case of initialization), or
     * a json object containing an encapsulated state observation.
     * This method deserializes the json object into a local state observation
     * instance.
     * @param msg Message received from server to be interpreted.
     * @throws IOException
    """
    def processLine(self,msg):
        try:
            if(msg == None):
                print ('Message is null.')

            message = msg.split(self.TOKEN_SEP)

            if (len(message) < 2):
                return

            self.lastMessageId = message[0]
            js = message[1]

            if (js == 'START'):
                self.sso.phase = SerializableStateObservation.Phase['START']
            else:
                self.sso = json.loads(js, object_hook=as_sso)
        except:
            print ('Line processing failed.')
            traceback.print_exc()

    """
     * Manages the start of the communication. It starts the whole process, and sets up the timer for the whole run.
    """
    def start(self):
        # insert timer stuff here...

        print ('Starting to play [OK]')
        self.startAgent()

    def startAgent(self):
        try:
            # do not currently know how to do this any better...
            self.player = AbstractPlayer()
        except:
            print ('Agent startup failed.')
            traceback.print_exc()

    """
     * Manages the init of a game played.
    """
    def init(self):
        # insert timer stuff here...
        ect = ElapsedCpuTimer()
        ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME)

        self.player.init(self.sso, ect.copy())

        if ect.exceededMaxTime():
            self.io.writeToServer(self.lastMessageId, "INIT_FAILED", self.LOG)
        else:
            self.io.writeToServer(self.lastMessageId, "INIT_DONE", self.LOG)

    """
     * Manages the action request for an agent. The agent is requested for an action,
     * which is sent back to the server
    """
    def act(self):
        # insert timer stuff here...
        ect = ElapsedCpuTimer()
        ect.setMaxTimeMillis(CompetitionParameters.ACTION_TIME)

        action = str(self.player.act(self.sso, ect.copy()))

        if ect.exceededMaxTime():
            if ect.elapsedMillis() > CompetitionParameters.ACTION_TIME_DISQ:
                self.io.writeToServer(self.lastMessageId, "END_OVERSPENT", self.LOG)
            else:
                self.io.writeToServer(self.lastMessageId, "ACTION_NIL", self.LOG)
        else:
            self.io.writeToServer(self.lastMessageId, action, self.LOG)

    """
     * Manages the aresult sent to the agent. The time limit for this call will be TOTAL_LEARNING_TIME
     * or EXTRA_LEARNING_TIME if current global time is beyond TOTAL_LEARNING_TIME.
     * The agent is assumed to return the next level to play. It will be ignored if
     *    a) All training levels have not been played yet (in which case the starting sequence 0-1-2 continues).
     *    b) It's outside the range [0,4] (in which case we play one at random)
     *    c) or we are in the validation phase (in which case the starting sequence 3-4 continues).
    """
    def result(self):
        # insert timer stuff here...
        ect = ElapsedCpuTimer()

        if not global_ect.exceededMaxTime():
            ect = global_ect.copy()
        else:
            ect.setMaxTimeMillis(CompetitionParameters.EXTRA_LEARNING_TIME)

        nextLevel = self.player.result(self.sso, ect.copy())

        if ect.exceededMaxTime():
            self.io.writeToServer(self.lastMessageId, "END_OVERSPENT", self.LOG)
        else:
            self.io.writeToServer(self.lastMessageId, nextLevel + '', self.LOG)
        



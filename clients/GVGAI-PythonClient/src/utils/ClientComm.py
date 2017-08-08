import importlib
import json
import logging
import sys
import os
import traceback

from utils.SerializableStateObservation import SerializableStateObservation, Phase, Observation

sys.path.append(os.path.dirname(os.path.realpath(__file__))+'/..')
sys.path.append('../sampleRandom')

from CompetitionParameters import CompetitionParameters
from ElapsedCpuTimer import ElapsedCpuTimer
from IOSocket import IOSocket
from Types import LEARNING_SSO_TYPE


class ClientComm:
    """
     * Client communication, set up the socket for a given agent
    """

    def __init__(self, agentName):
        self.TOKEN_SEP = '#'
        self.io = IOSocket(CompetitionParameters.SOCKET_PORT)
        self.sso = SerializableStateObservation()
        self.agentName = agentName
        self.lastMessageId = 0
        self.LOG = False
        self.player = None
        self.global_ect = None
        self.lastSsoType = LEARNING_SSO_TYPE.JSON

    def startComm(self):
        self.io.initBuffers()

        try:
            self.listen()
        except Exception as e:
            logging.exception(e)
            print("Start listen [FAILED]")
            traceback.print_exc()
            sys.exit()

    """
     * Method that perpetually listens for messages from the server.
     * With the use of additional helper methods, this function interprets
     * messages and represents the core response-generation methodology of the agent.
     * @throws IOException
    """

    def listen(self):
        line = ''

        while line is not None:
            line = self.io.readLine()
            line = line.rstrip("\r\n")
            self.processLine(line)

            if self.sso.phase == Phase.START:
                self.start()

            elif self.sso.phase == "INIT":
                self.sso.phase = Phase.INIT
                self.init()

            elif self.sso.phase == Phase.INIT:
                self.init()

            elif self.sso.phase == "END":
                self.sso.phase = Phase.END
                self.result()

            elif self.sso.phase == Phase.END:
                self.result()

            elif self.sso.phase == "ABORT":
                self.sso.phase = Phase.ABORT
                self.result()

            elif self.sso.phase == Phase.ABORT:
                self.result()

            elif self.sso.phase == "ACT":
                self.sso.phase = Phase.ACT
                self.act()

            elif self.sso.phase == Phase.ACT:
                self.act()

            elif self.sso.phase == Phase.FINISH:
                line = None

            elif self.sso.phase == "FINISH":
                line = None

            else:
                self.io.writeToServer(self.lastMessageId, 'ERROR', self.LOG)

    """
    Helper method that converts a given dictionary into
    a correct SSO type
    """

    def as_sso(self, d):
        self.sso.__dict__.update(d)
        return self.sso

    def parse_json(self, input):
        parsed_input = json.loads(input)
        self.sso.__dict__.update(parsed_input)
        if parsed_input.get('observationGrid'):
            self.sso.observationGrid = [[[None for j in range(self.sso.observationGridMaxCol)]
                                         for i in range(self.sso.observationGridMaxRow)]
                                        for k in range(self.sso.observationGridNum)]
            for i in range(self.sso.observationGridNum):
                for j in range(len(parsed_input['observationGrid'][i])):
                    for k in range(len(parsed_input['observationGrid'][i][j])):
                        self.sso.observationGrid[i][j][k] = Observation(parsed_input['observationGrid'][i][j][k])

        if parsed_input.get('NPCPositions'):
            self.sso.NPCPositions = [[None for j in
                                      range(self.sso.NPCPositionsMaxRow)] for i in
                                     range(self.sso.NPCPositionsNum)]
            for i in range(self.sso.NPCPositionsNum):
                for j in range(len(parsed_input['NPCPositions'][i])):
                    self.sso.NPCPositions[i][j] = Observation(parsed_input['NPCPositions'][i][j])

        if parsed_input.get('immovablePositions'):
            self.sso.immovablePositions = [[None for j in
                                            range(self.sso.immovablePositionsMaxRow)] for i in
                                           range(self.sso.immovablePositionsNum)]
            for i in range(self.sso.immovablePositionsNum):
                for j in range(len(parsed_input['immovablePositions'][i])):
                    self.sso.immovablePositions[i][j] = Observation(parsed_input['immovablePositions'][i][j])

        if parsed_input.get('movablePositions'):
            self.sso.movablePositions = [[None for j in
                                          range(self.sso.movablePositionsMaxRow)] for i in
                                         range(self.sso.movablePositionsNum)]
            for i in range(self.sso.movablePositionsNum):
                for j in range(len(parsed_input['movablePositions'][i])):
                    self.sso.movablePositions[i][j] = Observation(parsed_input['movablePositions'][i][j])

        if parsed_input.get('resourcesPositions'):
            self.sso.resourcesPositions = [[None for j in
                                            range(self.sso.resourcesPositionsMaxRow)] for i in
                                           range(self.sso.resourcesPositionsNum)]
            for i in range(self.sso.resourcesPositionsNum):
                for j in range(len(parsed_input['resourcesPositions'][i])):
                    self.sso.resourcesPositions[i][j] = Observation(parsed_input['resourcesPositions'][i][j])

        if parsed_input.get('portalsPositions'):
            self.sso.portalsPositions = [[None for j in
                                          range(self.sso.portalsPositionsMaxRow)] for i in
                                         range(self.sso.portalsPositionsNum)]
            for i in range(self.sso.portalsPositionsNum):
                for j in range(len(parsed_input['portalsPositions'][i])):
                    self.sso.portalsPositions[i][j] = Observation(parsed_input['portalsPositions'][i][j])

        if parsed_input.get('fromAvatarSpritesPositions'):
            self.sso.fromAvatarSpritesPositions = [[None for j in
                                                    range(self.sso.fromAvatarSpritesPositionsMaxRow)] for i in
                                                   range(self.sso.fromAvatarSpritesPositionsNum)]
            for i in range(self.sso.fromAvatarSpritesPositionsNum):
                for j in range(len(parsed_input['fromAvatarSpritesPositions'][i])):
                    self.sso.fromAvatarSpritesPositions[i][j] = Observation(parsed_input['fromAvatarSpritesPositions'][i][j])


    """
     * Method that interprets the received messages from the server's side.
     * A message can either be a string (in the case of initialization), or
     * a json object containing an encapsulated state observation.
     * This method deserializes the json object into a local state observation
     * instance.
     * @param msg Message received from server to be interpreted.
     * @throws IOException
    """

    def processLine(self, msg):
        try:
            if msg is None:
                print ("Message is null")
                return

            message = msg.split(self.TOKEN_SEP)
            if len(message) < 2:
                print ("Message not complete")
                return

            self.lastMessageId = message[0]
            js = message[1]

            self.sso = SerializableStateObservation()
            if js == "START":
                self.sso.phase = Phase.START
            elif js == "FINISH":
                self.sso.phase = Phase.FINISH
            else:
                js.replace('"', '')
                self.parse_json(js)
                # self.sso = json.loads(js, object_hook=self.as_sso)

            if self.sso.phase == "ACT":
                if self.lastSsoType == LEARNING_SSO_TYPE.IMAGE or self.lastSsoType == "IMAGE" \
                        or self.lastSsoType == LEARNING_SSO_TYPE.BOTH or self.lastSsoType == "BOTH":
                    if self.sso.imageArray:
                        self.sso.convertBytesToPng(self.sso.imageArray)

        except Exception as e:
            logging.exception(e)
            print("Line processing [FAILED]")
            traceback.print_exc()
            sys.exit()

    """
     * Manages the start of the communication. It starts the whole process, and sets up the timer for the whole run.
    """

    def start(self):
        self.global_ect = ElapsedCpuTimer()
        self.global_ect.setMaxTimeMillis(CompetitionParameters.TOTAL_LEARNING_TIME)
        ect = ElapsedCpuTimer()
        ect.setMaxTimeMillis(CompetitionParameters.START_TIME)
        self.startAgent()
        if ect.exceededMaxTime():
            self.io.writeToServer(self.lastMessageId, "START_FAILED", self.LOG)
        else:
            self.io.writeToServer(self.lastMessageId, "START_DONE" + "#" + self.lastSsoType, self.LOG)

    def startAgent(self):
        try:
            try:
                module = importlib.import_module(self.agentName, __name__)
                try:
                    self.player = getattr(module, 'Agent')()
                    self.lastSsoType = self.player.lastSsoType
                except AttributeError:
                    logging.error('ERROR: Class does not exist')
                    traceback.print_exc()
                    sys.exit()
            except ImportError:
                logging.error('ERROR: Module does not exist')
                traceback.print_exc()
                sys.exit()
            print("Agent startup [OK]")
        except Exception as e:
            logging.exception(e)
            print("Agent startup [FAILED]")
            traceback.print_exc()
            sys.exit()

    """
     * Manages the init of a game played.
    """

    def init(self):
        ect = ElapsedCpuTimer()
        ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME)
        self.player.init(self.sso, ect.copy())
        self.lastSsoType = self.player.lastSsoType
        if ect.exceededMaxTime():
            self.io.writeToServer(self.lastMessageId, "INIT_FAILED", self.LOG)
        else:
            self.io.writeToServer(self.lastMessageId, "INIT_DONE" + "#" + self.lastSsoType, self.LOG)

    """
     * Manages the action request for an agent. The agent is requested for an action,
     * which is sent back to the server
    """

    def act(self):
        ect = ElapsedCpuTimer()
        ect.setMaxTimeMillis(CompetitionParameters.ACTION_TIME)
        action = str(self.player.act(self.sso, ect.copy()))
        if (not action) or (action == ""):
            action = "ACTION_NIL"

        self.lastSsoType = self.player.lastSsoType
        if ect.exceededMaxTime():
            if ect.elapsedNanos() > CompetitionParameters.ACTION_TIME_DISQ*1000000:
                self.io.writeToServer(self.lastMessageId, "END_OVERSPENT", self.LOG)
            else:
                self.io.writeToServer(self.lastMessageId, "ACTION_NIL" + "#" + self.lastSsoType, self.LOG)
        else:
            self.io.writeToServer(self.lastMessageId, action + "#" + self.lastSsoType, self.LOG)

    """
     * Manages the aresult sent to the agent. The time limit for this call will be TOTAL_LEARNING_TIME
     * or EXTRA_LEARNING_TIME if current global time is beyond TOTAL_LEARNING_TIME.
     * The agent is assumed to return the next level to play. It will be ignored if
     *    a) All training levels have not been played yet (in which case the starting sequence 0-1-2 continues).
     *    b) It's outside the range [0,4] (in which case we play one at random)
     *    c) or we are in the validation phase (in which case the starting sequence 3-4 continues).
    """

    def result(self):
        ect = ElapsedCpuTimer()

        if not self.global_ect.exceededMaxTime():
            ect = self.global_ect.copy()
        else:
            ect.setMaxTimeMillis(CompetitionParameters.EXTRA_LEARNING_TIME)

        nextLevel = self.player.result(self.sso, ect.copy())
        # print "Result of a game at " + str(ect.remainingTimeMillis()) + "ms to the end."
        self.lastSsoType = self.player.lastSsoType
        if ect.exceededMaxTime():
            self.io.writeToServer(self.lastMessageId, "END_OVERSPENT", self.LOG)
        else:

            if self.global_ect.exceededMaxTime():
                end_message = "END_VALIDATION" if self.sso.isValidation else "END_TRAINING"
                self.io.writeToServer(self.lastMessageId, end_message, self.LOG)
            else:
                self.io.writeToServer(self.lastMessageId, str(nextLevel) + "#" + self.lastSsoType, self.LOG)

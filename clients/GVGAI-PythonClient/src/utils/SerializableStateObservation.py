import json

from Types import WINNER as WINNER
from Types import ACTIONS as ACTIONS
from CompetitionParameters import CompetitionParameters as CompetitionParameters
from PIL import Image
import io


class SerializableStateObservation:
    """
     * Serialized state observation, corresponding to the Java Client code:
     * GVGAI-JavaClient.src.serialization.SerializableStateObservation
    """
    def __init__(self):
        self.imageArray = bytearray([])
        
        self.phase = Phase()
        self.isValidation = True

        self.winner = WINNER()
        self.actions = ACTIONS()

        self.gameScore = 0.0
        self.gameTick = 0
        self.gameWinner = self.winner.NO_WINNER
        self.isGameOver = True
        self.worldDimension = []
        self.blockSize = 0

        self.noOfPlayers = 0
        self.avatarSpeed = 0.0
        self.avatarOrientation = []
        self.avatarPosition = []
        self.avatarLastAction = None  # self.actions.ACTION_NIL
        self.avatarType = 0
        self.avatarHealthPoints = 0
        self.avatarMaxHealthPoints = 0
        self.avatarLimitHealthPoints = 0
        self.isAvatarAlive = True
        self.availableActions = []
        self.avatarResources = {}

        self.observationGrid = []
        self.NPCPositionsNum = 0
        self.NPCPositionsMaxRow = 0
        self.NPCPositions = []
        self.immovablePositions = []
        self.immovablePositionsNum = 0
        self.immovablePositionsMaxRow = 0
        self.movablePositions = []
        self.resourcesPositions = []
        self.portalsPositions = []
        self.fromAvatarSpritesPositions = []

    def convertBytesToPng(self, pixels):
        for i, e in enumerate(pixels):
            pixels[i] = e & 0xFF
        image = Image.open(io.BytesIO(bytearray(pixels)))
        image.save(CompetitionParameters.SCREENSHOT_FILENAME)


class Phase:
    """
     * Used to control the communication between server and client, corresponding to the Java Client code:
     * GVGAI-JavaClient.src.serialization.SerializableStateObservation
    """
    def __init__(self):
        pass

    START, INIT, ACT, ABORT, END, FINISH = range(6)


class Observation:
    """
     * Serialized state observation, corresponding to the Java Client code:
     * GVGAI-JavaClient.src.serialization.Observation
    """

    def __init__(self, parse_Observation = None):
        if parse_Observation is None:
            self.category = -1
            self.itype = -1
            self.obsID = -1
            self.position = Vector2d()
            self.reference = Vector2d()
            self.sqDist = -1
        else:
            self.category = parse_Observation['category']
            self.itype = parse_Observation['itype']
            self.obsID = parse_Observation['obsID']
            self.position = Vector2d(parse_Observation['position'])
            self.reference = Vector2d(parse_Observation['reference'])
            self.sqDist = parse_Observation['sqDist']

class Vector2d:
    """
     * Serialized state observation, corresponding to the Java Client code:
     * GVGAI-JavaClient.src.serialization.Vector2d
    """

    def __init__(self, position = None):
        if position is None:
            self.x = -1
            self.y = -1
        else:
            self.x = position['x']
            self.y = position['y']

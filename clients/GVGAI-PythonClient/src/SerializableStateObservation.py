from enum import Enum
from Types import *

class SerializableStateObservation:

    def __init__(self):
        self.phase = Phase()
        self.isValidation = True
        
        self.gameScore = 0.0
        self.gameTick = 0
        self.gameWinner = Types.WINNER['NO_WINNER']
        self.isGameOver = True
        self.worldDimension = []
        self.blockSize = 0

        self.avatarSpeed = 0.0
        self.avatarOrientation = []
        self.avatarLastAction = None  # Types.ACTIONS['ACTION_NIL']
        self.avatarType = 0
        self.avatarHealthPoints = 0
        self.avatarMaxHealthPoints = 0
        self.avatarLimitHealthPoints = 0
        self.isAvatarAlive = True
        self.availableActions = []
        self.avatarResources = {}

        observationGrid = []
        NPCPositions = []
        immovablePositions = []
        movablePositions = []
        resourcesPositions = []
        portalsPositions = []
        fromAvatarSpritePositions = []

class Phase(Enum):
    START = 0
    INIT = 1
    ACT = 2
    ABORT = 3
    END = 4

        
    

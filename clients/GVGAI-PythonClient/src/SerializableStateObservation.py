from enum import Enum


class SerializableStateObservation:

    def __init__(self):
        self.phase = Phase()
        self.isValidation = True
        
        self.gameScore = 0.0
        self.gameTick = 0
        self.gameWinner =
        self.isGameOver = True
        self.worldDimension = []
        self.blockSize = 0

        self.avatarSpeed = 0.0
        self.avatarOrientation = []
        self.avatarLastAction =
        self.avatarType = 0
        self.avatarHealthPoints = 0
        self.avatarMaxHealthPoints = 0
        self.avatarLimitHealthPoints = 0
        self.isAvatarAlive = True
        self.availableActions = [[]]
        self.avatarResources = {}

class Phase(Enum):
    START = 1
    INIT = 2
    ACT = 3
    ABORT = 4
    END = 5

        
    

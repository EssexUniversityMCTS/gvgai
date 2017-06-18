from enum import Enum
from Types import WINNER as WINNER
from Types import ACTIONS as ACTIONS

class SerializableStateObservation:

    def __init__(self):
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

        self.avatarSpeed = 0.0
        self.avatarOrientation = []
        self.avatarLastAction = None  # self.actions.ACTION_NIL
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

class Phase:
    START, INIT, ACT, ABORT, END = range(5)

        
    

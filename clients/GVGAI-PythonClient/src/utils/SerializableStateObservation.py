from Types import WINNER as WINNER
from Types import ACTIONS as ACTIONS
from PIL import Image
import io


class SerializableStateObservation:
    """
     * Serialized state observation, corresponding to the Java Client code:
     * GVGAI-JavaClient.src.serialization.SerializableStateObservation
    """
    def __init__(self):
        self.imageArray = bytes([])
        
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
        self.NPCPositions = []
        self.immovablePositions = []
        self.movablePositions = []
        self.resourcesPositions = []
        self.portalsPositions = []
        self.fromAvatarSpritePositions = []

    def convertBytesToPng(self, pixels):
        stream = io.BytesIO(pixels)
        image = Image.open(stream)
        image.save("gamestateByBytes.png")

        # image = Image.open(io.BytesIO(pixels))
        # image.save("gamestateByBytes.png")

class Phase:
    """
     * Used to control the communication between server and client, corresponding to the Java Client code:
     * GVGAI-JavaClient.src.serialization.SerializableStateObservation
    """
    def __init__(self):
        pass

    START, INIT, ACT, ABORT, END, FINISH = range(6)

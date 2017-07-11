from enum import Enum

class ACTIONS:
    """
     * All action types, corresponding to the server Java code ontology.Types
    """

    def __init__(self):
        pass

    ACTION_NIL = 0
    ACTION_UP = 1
    ACTION_LEFT = 2
    ACTION_DOWN = 3
    ACTION_RIGHT = 4
    ACTION_USE = 5
    ACTION_ESCAPE = 6


class WINNER:
    """
     * Winner/Loser types, corresponding to the server Java code ontology.Types
    """

    def __init__(self):
        pass

    PLAYER_DISQ = -100
    NO_WINNER = -1
    PLAYER_LOSES = 0
    PLAYER_WINS = 1


class LEARNING_SSO_TYPE(Enum):
    IMAGE = 0
    JSON = 1
    BOTH = 2

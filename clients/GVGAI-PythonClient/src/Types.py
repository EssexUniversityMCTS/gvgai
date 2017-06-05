from enum import Enum

class ACTIONS(Enum):
    ACTION_NIL = 1
    ACTION_UP = 2
    ACTION_LEFT = 3
    ACTION_DOWN = 4
    ACTION_RIGHT = 5
    ACTION_USE = 6
    ACTION_ESCAPE = 7

class WINNER(Enum):
    PLAYER_DISQ = -100
    NO_WINNER = -1
    PLAYER_LOSES = 0
    PLAYER_WINS = 1

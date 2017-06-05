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
    -100 = PLAYER_DISQ
    -1 = NO_WINNER
    0 =PLAYER_LOSES
    1 = PLAYER_WINS

    def __init__(self,value):
        self.key = self[value]
    
    def key():
        return self.key

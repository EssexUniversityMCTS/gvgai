import sys


class CompetitionParameters:
    """
     * Competition parameters, should be the same with the ones on Server side:
     * refer to core.competition.CompetitionParameters
    """
    def __init__(self):
        pass

    if 'win32' in sys.platform:
        OS_WIN = True
    else:
        OS_WIN = False

    USE_SOCKETS = True
    START_TIME = 1000
    INITIALIZATION_TIME = 1000
    ACTION_TIME = 40
    ACTION_TIME_DISQ = 50
    MILLIS_IN_MIN = 60*1000
    TOTAL_LEARNING_TIME = 5*MILLIS_IN_MIN
    EXTRA_LEARNING_TIME = 1000
    SOCKET_PORT = 8080
    SCREENSHOT_FILENAME = "gameStateByBytes.png"
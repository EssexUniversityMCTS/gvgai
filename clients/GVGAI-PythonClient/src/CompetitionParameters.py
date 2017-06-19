import sys


class CompetitionParameters:
    if 'win32' in sys.platform:
        OS_WIN = True
    else:
        OS_WIN = False

    START_TIME = 1000
    INITIALIZATION_TIME = 1000
    ACTION_TIME = 40
    ACTION_TIME_DISQ = 50
    MILLIS_IN_MIN = 60*1000
    TOTAL_LEARNING_TIME = 5*MILLIS_IN_MIN
    EXTRA_LEARNING_TIME = 1000
    USE_SOCKETS = True
    SOCKET_PORT = 3000

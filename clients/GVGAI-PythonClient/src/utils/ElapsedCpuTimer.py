import time
import sys


class ElapsedCpuTimer:
    """
     * Timer, corresponding to the server Java code tools.ElapsedCpuTimer
    """

    def __init__(self):
        self.oldTime = self.getTime()
        self.maxTime = 0

    if sys.platform.startswith('win32'):
        OS_WIN = True
    else:
        OS_WIN = False

    def copy(self):
        newCpuTimer = ElapsedCpuTimer()
        newCpuTimer.maxTime = self.maxTime
        newCpuTimer.oldTime = self.oldTime
        return newCpuTimer

    def elapsed(self):
        return self.getTime() - self.oldTime

    def elapsedNanos(self):
        return self.elapsed()

    def elapsedMillis(self):
        return self.elapsed() / 1000000

    def elapsedSeconds(self):
        return self.elapsedMillis()/1000

    def elapsedMinutes(self):
        return self.elapsedSeconds() / 60.0

    def elapsedHours(self):
        return self.elapsedMinutes() / 60

    def getTime(self):
        return time.time()*1000000000

    """
     * Return current time in millesecond
    """
    def getCpuTime(self):
        return int(round(time.time() * 1000))

    def setMaxTimeMillis(self, timeToSet):
        self.maxTime = timeToSet * 1000000
        self.oldTime = self.getTime()

    def remainingTimeMillis(self):
        diff = self.maxTime - self.elapsed()
        return diff

    def exceededMaxTime(self):
        return self.elapsed() > self.maxTime

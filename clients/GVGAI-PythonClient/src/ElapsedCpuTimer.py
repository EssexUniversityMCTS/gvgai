import time

class ElapsedCpuTimer:
    if 'win32' in sys.platform():
        OS_WIN = True
    else:
        OS_WIN = False

	def __init__(self):
		self.oldTime = self.getTime()
		self.maxTime = 0

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
		return self.elapsed()/1000000.0
		
	def elapsedSeconds(self):
		return self.elapsedMillis()/1000.0
		
	def elapsedMinutes(self):
		return self.elapsedMillis()/1000.0/60.0
		
	def elapsedHours(self):
		return self.elapsedMinutes()/60
		
	def getTime(self):
		return self.getCpuTime()
		
	def getCpuTime(self):
		return time.clock()
		
	def setMaxTimeMillis(self, timeToSet):
		self.maxTime = timeToSet * 1000000
		
	def remainingTimeMillis(self):
		diff = self.maxTime - self.elapsed()
		return diff / 1000000.0
		
	def exceededMaxTime(self):
		return (self.elapsed() > self.maxTime)
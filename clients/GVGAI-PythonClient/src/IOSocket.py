import socket
import sys
import os

from ClientComm import ClientComm

class IOSocket:
    
    def __init__(self,port):  
        self.port = port
        self.hostname = 'localhost'

    def initBuffers():
        connected = False
        self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        while not connected:
            try:
                self.s.bind(hostname, port)
                connected = True
                print 'Client connected to server [OK]'
            except socket.error as msg:
                print 'Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1]
                sys.exit()
                
    def writeToFile(line):
        with open('logs/clientLog.txt','w') as file:
            file.write(line + os.linesep)
            file.flush()

    def writeToServer(self, messageId, line, log):
        msg = messageId + ClientComm.TOKEN_SEP + line
        self.s.send(msg)
        if log:
            writeToFile(msg)

    def readLine(self):
        return self.s.recv(RECV_BUFFER)

    def writeToServer(self, line):
        try:
            self.s.send(line + os.linesep)
            self.s.flush()
        except:
            print 'Error trying to write ' + file + ' to server.'

    

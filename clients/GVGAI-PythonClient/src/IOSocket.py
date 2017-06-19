import socket
import sys, traceback
import os

import ClientComm


class IOSocket:
    
    def __init__(self,port):  
        self.port = port
        self.hostname = '127.0.0.1'

    def initBuffers(self):
        self.connected = False
        self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        while not self.connected:
            try:
                print ("Selected host is: " + str(self.hostname))
                print ("Selected port is: " + str(self.port))
                self.s.connect((self.hostname, self.port))
                self.connected = True
                print ('Client connected to server [OK]')
            except:
                traceback.print_exc()
                sys.exit()
                
    def writeToFile(line):
        with open('logs/clientLog.txt','w') as file:
            file.write(line + os.linesep)
            file.flush()

    def writeToServer(self, messageId, line, log):
        msg = messageId + ClientComm.TOKEN_SEP + line
        self.s.send(bytes(msg))
        if log:
            writeToFile(msg)

    def readLine(self):
        return self.s.recv(2048)

    def writeToServer(self, line):
        try:
            self.s.send(line + os.linesep)
            self.s.flush()
        except:
            print ('Error trying to write ' + file + ' to server.')


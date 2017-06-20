import logging
import os
import socket
import sys
import traceback

import time


class IOSocket:
    """
     * Socket for communication
    """

    def __init__(self, port):
        self.BUFF_SIZE = 1024000
        self.TOKEN_SEP = '#'
        self.port = port
        self.hostname = "127.0.0.1"
        self.logfilename = "./logs/clientLog.txt"
        self.connected = False
        self.socket = None
        self.logfile = open(self.logfilename, "a")

    def initBuffers(self):
        while not self.connected:
            try:
                self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                print ("Selected host is: " + str(self.hostname))
                print ("Selected port is: " + str(self.port))
                self.socket.connect((self.hostname, self.port))
                self.connected = True
                print ("Client connected to server [OK]")
            except Exception as e:
                logging.exception(e)
                print("Client connected to server [FAILED]")
                traceback.print_exc()
                sys.exit()

    def writeToFile(self, line):
        sys.stdout.write(line + os.linesep)
        self.logfile.write(line + os.linesep)
        sys.stdout.flush()
        self.logfile.flush()

    def writeToServer(self, messageId, line, log):
        msg = str(messageId) + self.TOKEN_SEP + line + "\n"
        print(msg)

        try:
            self.socket.send(bytes(msg))
            if log:
                self.writeToFile(msg)
        except Exception as e:
            logging.exception(e)
            print ("Write " + self.logfilename + " to server [FAILED]")
            traceback.print_exc()
            sys.exit()

    def readLine(self):
        try:
            # return self.recv_timeout
            return self.socket.recv(1024000)
        except Exception as e:
            logging.exception(e)
            print ("Read from server [FAILED]")
            traceback.print_exc()
            sys.exit()

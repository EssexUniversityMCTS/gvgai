import logging
import os
import socket
import sys
import traceback


class IOSocket:
    """
     * Socket for communication
    """

    def __init__(self, port):
        self.TOKEN_SEP = '#'
        self.port = port
        self.hostname = "127.0.0.1"
        self.logfilename = "logs/clientLog.txt"
        self.connected = False
        self.s = None

    def initBuffers(self):
        while not self.connected:
            try:
                self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                print ("Selected host is: " + str(self.hostname))
                print ("Selected port is: " + str(self.port))
                self.s.connect((self.hostname, self.port))
                self.connected = True
                print ("Client connected to server [OK]")
            except Exception as e:
                logging.exception(e)
                print("Client connected to server [FAILED]")
                traceback.print_exc()
                sys.exit()

    def writeToFile(self, line):
        with open(self.logfilename, "w") as logfile:
            logfile.write(line + os.linesep)
            logfile.flush()

    def writeToServer(self, messageId, line, log):
        msg = str(messageId) + self.TOKEN_SEP + line
        try:
            self.s.send(bytes(msg))
            print("DEBUG: " + msg)  # todo
            print("DEBUG: " + bytes(msg))  # todo
            if log:
                self.writeToFile(msg)
        except Exception as e:
            logging.exception(e)
            print ("Write " + self.logfilename + " to server [FAILED]")
            traceback.print_exc()
            sys.exit()

    def readLine(self):
        try:
            return self.s.recv(2048)
        except Exception as e:
            logging.exception(e)
            print ("Read from server [FAILED]")
            traceback.print_exc()
            sys.exit()

    # def writeToServer(self, line):
    #     try:
    #         self.s.send(line + os.linesep)
    #         self.s.flush()
    #     except Exception as e:
    #         logging.exception(e)
    #         print ("Error trying to write " + self.logfilename + " to server.")

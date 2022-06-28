import socket
from threading import Thread
import time

def getLocalIP():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    ip = s.getsockname()[0]
    s.close()
    return ip

class serverSocket:
    def __init__(self):
        self.regClientLocalList = {}
        self.regClientGlobalList = {}
        self.regClientTime = {}
        self.serverSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    def parseIP(self, addr):
        return addr.split(':')[0]

    def sendToMakeAddr(self, addr):
        addr = addr.split(':')
        return (addr[0], int(addr[1]))

    def makeMsg(self, event, clientID, clientAddr):
        msg = 'server' + '_' + event + '_' + clientID + '_' + clientAddr
        return msg

    def isSameNAT(self, addr_1, addr_2):
        IP_1 = self.parseIP(addr_1)
        IP_2 = self.parseIP(addr_2)
        IP_1 = IP_1.split('.')[:-1]
        IP_2 = IP_2.split('.')[:-1]
        if IP_1 == IP_2:
            return True
        else:
            return False

    def sendClientList(self, clientID, addr):
        for key in self.regClientTime:
            if self.isSameNAT(self.regClientLocalList[clientID], self.regClientLocalList[key]):
                clientAddr = self.regClientLocalList[key]
            else:
                clientAddr = self.regClientGlobalList[key]
            msg = self.makeMsg('register', key, clientAddr)
            self.serverSocket.sendto(msg.encode(), self.sendToMakeAddr(addr))


    def recvMsg(self):
        while True:
            try:
                # client msg = 'client' + '_' + event + '_' + self.clientID + '_' + self.localIP + ':' + str(10081)
                msg, clientAddr = self.serverSocket.recvfrom(2048)
                clientAddr = clientAddr[0] + ':' + str(clientAddr[1])
                splitMsg = msg.decode().split('_')
                event = splitMsg[1]
                clientID = splitMsg[2]
                localAddr = splitMsg[3]

                print(clientID + '  ' + event + '  local: ' + localAddr + '  global: ' + clientAddr)
                if 'Keep Alive' in splitMsg:
                    self.regClientTime[clientID] = time.time()
                elif 'unregister' in splitMsg:
                    self.sendResponse(clientID, event)
                    self.delElement(clientID)
                elif 'register' in splitMsg:
                    print(clientID + '  local: ' + localAddr + '  global: ' + clientAddr)
                    self.addElement(clientID, localAddr, clientAddr)
                    self.sendResponse(clientID, event)
                    self.sendClientList(clientID, clientAddr)
            except:
                pass


    def sendResponse(self, clientID, event):
        for key in self.regClientGlobalList:
            if key == clientID and event == 'register':
                continue
            sendAddr = self.regClientGlobalList[key]
            if self.isSameNAT(self.regClientLocalList[clientID], self.regClientLocalList[key]):
                clientAddr = self.regClientLocalList[clientID]
            else:
                clientAddr = self.regClientGlobalList[clientID]
            msg = self.makeMsg(event, clientID, clientAddr)
            self.serverSocket.sendto(msg.encode(), self.sendToMakeAddr(sendAddr))

    def addElement(self, clientID, localAddr, globalAddr):
        self.regClientTime[clientID] = time.time()
        self.regClientLocalList[clientID] = localAddr
        self.regClientGlobalList[clientID] = globalAddr

    def delElement(self, clientID):
        del self.regClientTime[clientID]
        del self.regClientLocalList[clientID]
        del self.regClientGlobalList[clientID]

    def timeOut(self):
        while True:
            try:
                for clientID in self.regClientTime:
                    if time.time() - self.regClientTime[clientID] >= 30:
                        print(clientID + ' is offline')
                        self.sendResponse(clientID, 'offline')
                        self.delElement(clientID)
            except:
                continue


if __name__ == '__main__':
    server = serverSocket()
    localIP = getLocalIP()
    server.serverSocket.bind((localIP, 10080))
    serverAddr = server.serverSocket.getsockname()
    print("Address = {}".format(serverAddr))
    threadTimeOut = Thread(target=server.timeOut, args=())
    threadRecvMsg = Thread(target=server.recvMsg, args=())
    threadTimeOut.start()
    threadRecvMsg.start()
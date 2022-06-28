import socket
from threading import Thread
import time

def getLocalIP():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    ip = s.getsockname()[0]
    s.close()
    return ip

class clientSocket():
    def __init__(self, clientID, serverIP):
        self.clientID = clientID
        self.serverIP = serverIP
        self.localSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.globalSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.localIP = getLocalIP()
        self.regClientList = {}
        self.online = False

    def makeMsg(self, event):
        msg = 'client' + '_' + event + '_' + self.clientID + '_' + self.localIP + ':' + str(10081)
        return msg

    def parseMsg(self, msg):
        # servermsg = 'server' + '_' + event + '_' + clientID + '_' + clientAddr
        msg = msg.decode()
        msg = msg.split('_')

        if 'server' in msg:
            event = msg[1]
            clientID = msg[2]
            clientAddr = msg[3]
            if event == 'register':
                self.regClientList[clientID] = clientAddr
                print(clientID + ' is register at IP: ' + clientAddr)
            elif event == 'unregister':
                if clientID == self.clientID:
                    self.online = False
                    self.localSocket.close()
                    self.globalSocket.close()
                    return
                del self.regClientList[clientID]
                print(clientID + ' is unregistered')
            elif event == 'offline':
                del self.regClientList[clientID]
                print(clientID + ' is off-line')
        else:
            print('From  ' + msg[0] + '  [' + msg[1] + ']')

    def unregister(self):
        msg = self.makeMsg('unregister')
        self.localSocket.sendto(msg.encode(), (self.serverIP, 10080))
        self.online = False

    def isSameNAT(self, IP_1, IP_2):
        IP_1 = IP_1.split('.')[:-1]
        IP_2 = IP_2.split('.')[:-1]
        if IP_1 == IP_2:
            return True
        else:
            return False

    def sendChat(self, chatMsg):
        recvClientID = chatMsg[0]
        msg = self.clientID + '_' + ' '.join(chatMsg[1:])

        recvClientAddr = self.regClientList[recvClientID].split(':')
        recvClientIP = recvClientAddr[0]
        recvClientPort = recvClientAddr[1]
        if self.isSameNAT(self.localIP, recvClientIP):
            self.localSocket.sendto(msg.encode(), (recvClientIP, int(recvClientPort)))
        else:
            self.globalSocket.sendto(msg.encode(), (recvClientIP, int(recvClientPort)))


    def runQuery(self, query):
        query = query.split()
        if query[0] == '@show_list':
            for clientID in self.regClientList:
                print(clientID + "  " + self.regClientList[clientID])
        elif query[0] == '@chat':
            self.sendChat(query[1:])
        elif query[0] == '@exit':
            self.unregister()

    def keepAlive(self):
        startTime = time.time()
        while self.online:
            nowTime = time.time()
            if nowTime - startTime >= 10:
                startTime = nowTime
                msg = self.makeMsg('Keep Alive')
                self.globalSocket.sendto(msg.encode(), (self.serverIP, 10080))

    def recvLocal(self):
        self.localSocket.bind((self.localIP, 10081))
        while self.online:
            recvMsg = self.localSocket.recv(2048)
            self.parseMsg(recvMsg)

    def recvGlobal(self):
        self.online = True
        msg = self.makeMsg('register')
        self.globalSocket.sendto(msg.encode(), (self.serverIP, 10080))
        while self.online:
            recvMsg = self.globalSocket.recv(2048)
            self.parseMsg(recvMsg)


if __name__ == '__main__':
    clientID = input("clientID: ")
    serverIP = input("serverIP: ")
    client = clientSocket(clientID, serverIP)
    threadLocal = Thread(target=client.recvLocal, args=())
    threadGlobal = Thread(target=client.recvGlobal, args=())
    threadAlive = Thread(target=client.keepAlive, args=())
    threadLocal.daemon = True
    threadGlobal.daemon = True
    threadAlive.daemon = True

    threadGlobal.start()
    threadLocal.start()
    threadAlive.start()


    while client.online:
        query = input()
        client.runQuery(query)
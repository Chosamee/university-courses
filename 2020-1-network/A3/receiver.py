import sys
import time
import socket
from threading import Thread

"Use this method to write Packet log"
def writePkt(logFile, startTime, pktNum, event):
    procTime = time.time() - startTime
    logFile.write('{:1.3f} pkt: {} | {}\n'.format(procTime, pktNum, event))
    logFile.flush()

"Use this method to write ACK log"
def writeAck(logFile, startTime, ackNum, event):
    procTime = time.time() - startTime
    logFile.write('{:1.3f} ACK: {} | {}\n'.format(procTime, ackNum, event))
    logFile.flush()

"Use this method to write final throughput log"
def writeEnd(logFile, throughput):
    logFile.write('File transfer is finished.')
    logFile.write('Throughput : {:.2f} pkts/sec\n'.format(throughput))
    logFile.flush()


class recv_handler:
    def __init__(self, receiver_socket, dstFileName, recvAddr, logFile, startTime):
        self.receiver_socket = receiver_socket
        self.dstFileName = dstFileName
        self.recvAddr = recvAddr
        self.packet_queue = []
        self.logFile = logFile
        self.startTime = startTime

    def file_receive(self):
        while True:
            buffer = self.receiver_socket.recv(2048)
            if buffer == b'\0':
                break

            try:
                pktnum = int(buffer[:10].decode())
                recv_data = buffer[10:]
            except:
                pass

            writePkt(self.logFile, self.startTime, pktnum, "recieved")

            if pktnum == len(self.packet_queue):
                self.packet_queue.append(recv_data)
                for_send_ack = str(len(self.packet_queue) - 1)
                self.receiver_socket.sendto(for_send_ack.encode(), self.recvAddr)
                writeAck(self.logFile, self.startTime, pktnum, "sent")

            else:
                for_send_ack = str(len(self.packet_queue) - 1)
                self.receiver_socket.sendto(for_send_ack.encode(), self.recvAddr)
                writeAck(self.logFile, self.startTime, len(self.packet_queue) - 1, "sent")

    def write_file(self):
        recv_file = open(self.dstFileName, 'wb')
        for data in self.packet_queue:
            recv_file.write(data)

    def run_recv(self):
        self.file_receive()
        self.write_file()
        writeEnd(self.logFile, len(self.packet_queue) / (time.time() - self.startTime))
        self.logFile.close()


def fileReceiver(receiver_socket, dstFileName, recvAddr, logFile, startTime):
    th1 = recv_handler(receiver_socket, dstFileName, recvAddr, logFile, startTime)
    th1.run_recv()


if __name__=='__main__':

    receiver_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    receiver_socket.bind(('', 10080))

    dstFileName, recvAddr = receiver_socket.recvfrom(2048)
    startTime = time.time()

    dstFileName = dstFileName.decode()
    logFile = open(dstFileName + '_receiving_log.txt', 'w')

    writeAck(logFile, startTime, -1, "received")

    receiver_socket.sendto(str(-1).encode(), recvAddr)

    writeAck(logFile, startTime, -1, "sent")

    th = Thread(target=fileReceiver, args=(receiver_socket, dstFileName, recvAddr, logFile, startTime))
    th.start()
import sys
import time
import socket
from threading import Thread
from collections import defaultdict

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
def writeEnd(logFile, throughput, avgRTT):
    logFile.write('File transfer is finished.\n')
    logFile.write('Throughput : {:.2f} pkts/sec\n'.format(throughput))
    logFile.write('Average RTT : {:.3f} ms\n'.format(avgRTT))
    logFile.flush()


def make_packet(pktnum, data):
    pktnum = str(pktnum)
    header = '0' * (10 - len(pktnum)) + str(pktnum)
    header = header.encode()
    data = header + data
    return data


class timer:
    def __init__(self, logFile, startTime):
        self.sendtime = []
        self.retransmit = defaultdict(int)
        self.RTO = 1
        self.SRTT = 0
        self.RTTVAR = 0
        self.first_try = True
        self.logFile = logFile
        self.startTime = startTime

    def save_sendtime(self, ack):
        if len(self.sendtime) == ack:
            self.sendtime.append(time.time())
        else:
            self.retransmit[ack] = 1
            self.sendtime[ack] = time.time()

    def time_out(self, recent_ack):
        try:
            if time.time() - self.sendtime[recent_ack + 1] >= self.RTO:
                return True
            else:
                return False
        except:
            return False

    def RTTcal(self, ack):
        if self.retransmit[ack] != 1:
            sampleRTT = time.time() - self.sendtime[ack]
            if self.SRTT == 0 and self.first_try:
                self.SRTT = sampleRTT
                self.RTTVAR = sampleRTT / 2
                self.first_try = False
            else:
                self.SRTT = sampleRTT * 0.125 + self.SRTT * 0.875
                self.RTTVAR = 0.75 * self.RTTVAR + 0.25 * abs(self.SRTT - sampleRTT)
            writeAck(self.logFile, self.startTime, ack, 'avgRTT = {:.3f}'.format(self.SRTT))

        self.RTO = self.SRTT + max(0.001, 4*self.RTTVAR)
        if self.RTO > 60:
            self.RTO = 60
        elif self.RTO < 0.200:
            self.RTO = 0.200



class send_handler:
    def __init__(self, sender_socket, recvAddr, windowSize, srcFilename, dstFilename, logFile, startTime, packet_queue):
        self.sender_socket = sender_socket
        self.recvAddr = (recvAddr, 10080)
        self.windowSize = windowSize
        self.srcFilename = srcFilename
        self.dstFilename = dstFilename
        self.logFile = logFile
        self.packet_queue = packet_queue
        self.startTime = startTime
        self.recent_ack = -2
        self.duplicate_ack = -1
        self.ack_packet = -1
        self.duplicate_lock = False
        self.timer = timer(logFile, startTime)
        self.lock = False
        self.retransmit_lock = False

    def send_to(self):
        sender_socket.send(dstFilename.encode())
        writePkt(self.logFile, self.startTime, -1, "sent")
        break_time = time.time()
        while self.recent_ack != -1:
            if time.time() - break_time > self.timer.RTO:
                sender_socket.send(dstFilename.encode())
                writePkt(self.logFile, self.startTime, -1, "sent")
                break_time = time.time()
        while True:
            recent_ack = self.recent_ack
            if self.recent_ack == len(self.packet_queue) - 1:
                while True:
                    try:
                        self.sender_socket.send(b'\0')
                    except:
                        break
                break
            if self.lock:
                if len(self.timer.sendtime) - 1 == recent_ack:
                    self.lock = False
                if self.timer.time_out(recent_ack):
                    writePkt(self.logFile, self.startTime, recent_ack + 1, 'timeout since {:.3f}(timeout value is {:.3f})'.format(self.timer.sendtime[recent_ack]-self.startTime, self.timer.RTO))
                    self.timer.RTO = 2 * self.timer.RTO

                    if self.timer.RTO >= 60:
                        self.timer.RTO = 60
                    self.retransmit_lock = True
                    self.lock = False
            else:
                for i in range(self.windowSize):
                    ack_packet = i + recent_ack + 1
                    if ack_packet >= len(self.packet_queue):
                        break

                    packet = make_packet(ack_packet, self.packet_queue[ack_packet])
                    self.timer.save_sendtime(ack_packet)
                    self.sender_socket.send(packet)
                    if self.retransmit_lock:
                        writePkt(self.logFile, self.startTime, ack_packet, "retransmitted")
                        self.retransmit_lock = False
                    else:
                        writePkt(self.logFile, self.startTime, ack_packet, "sent")
                if self.recent_ack == len(self.packet_queue) - 1:
                    self.sender_socket.send(b'\0')
                    break

                self.lock = True

    def recv_from(self):
        while True:
            recv_ack = int(self.sender_socket.recv(2048).decode())
            writeAck(self.logFile, self.startTime, recv_ack, "recieved")

            if recv_ack != self.recent_ack:
                self.duplicate_ack = 0

            else:
                self.duplicate_ack += 1

                if self.duplicate_ack == 3 and not self.duplicate_lock:
                    writeAck(self.logFile, self.startTime, recv_ack, "3 duplicated ACKs")
                    self.recent_ack = recv_ack
                    self.duplicate_ack = 0
                    self.duplicate_lock = True
                    self.lock = False
                    self.retransmit_lock = True

            if self.recent_ack < recv_ack:
                self.duplicate_lock = False
                self.recent_ack = recv_ack
                if self.recent_ack >= 0:
                    self.timer.RTTcal(self.recent_ack)

            if self.recent_ack == len(self.packet_queue) - 1:
                break

    def run_send(self):
        send_th = Thread(target=self.send_to, args=())
        recv_th = Thread(target=self.recv_from, args=())
        send_th.start()
        recv_th.start()
        send_th.join()
        recv_th.join()
        writeEnd(self.logFile, len(self.packet_queue)/(time.time() - self.startTime), self.timer.SRTT * 1000)


def fileSender(recvAddr, windowSize, srcFilename, dstFilename):

    send_file = open(srcFilename, "rb")
    logFile = open(srcFilename + '_sending_log.txt', 'w')

    packet_queue = []

    buffer = send_file.read(1024)
    while buffer:
        packet_queue.append(buffer)
        buffer = send_file.read(1024)

    send_file.close()
    startTime = time.time()

    ft = send_handler(sender_socket, recvAddr, windowSize, srcFilename, dstFilename, logFile, startTime, packet_queue)
    ft.run_send()

    logFile.close()




if __name__=='__main__':
    recvAddr = sys.argv[1]  #receiver IP address
    windowSize = int(sys.argv[2])   #window size
    srcFilename = sys.argv[3]   #source file name
    dstFilename = sys.argv[4]   #result file name

    # recvAddr = '127.0.0.1' #receiver IP address
    # windowSize = 32   #window size
    # srcFilename = 'CA3.pptx'   #source file name
    # dstFilename = 'a.pptx'   #result file name

    sender_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sender_socket.connect((recvAddr, 10080))

    th = Thread(target=fileSender, args=(recvAddr, windowSize, srcFilename, dstFilename))
    th.start()
from threading import Thread

import time
data_size = 10240
f = open("log.txt", 'a')


def copy_file(run_time, file_name, new_file_name):

    f.write("%.2f\tStart copying %s to %s \n" % (time.time()-run_time, file_name, new_file_name))

    fptr = open(file_name, 'rb')
    fptr2 = open(new_file_name, 'wb')

    while True:
        packet = fptr.read(data_size)
        if not packet:
            break
        fptr2.write(packet)

    f.write("%.2f\t%s is copied completely \n" % (time.time()-run_time, new_file_name))

    fptr.close()
    fptr2.close()


if __name__ == '__main__':
    run_time = time.time()

    while True:
        file_name = input("Input the file name: ")
        if file_name == "exit":
            break
        else:
            new_file_name = input("Input the new name: ")
            if new_file_name == "exit":
                break
            else:
                th1 = Thread(target=copy_file, args=(run_time, file_name, new_file_name))
                th1.start()
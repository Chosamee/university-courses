import sys
import time
from socket import *
from threading import Thread
import time



user_data = {}
i = 0


class handler:
    def __init__(self):
        self.msg = None

    def response(self, msg):
        content = ''.encode()
        path = self.msg_to_path(msg)
        real_path, response_code, content_type, user, a = self.parse_path(path)
        start_time = None

        if real_path == '/cookie.html' or real_path == '/secret.html':
            try:
                userid = path[-3].split('=')[1]
                start_time = path[-2].split('=')[1]
            except:
                if not a:
                    response_code = 403

            if response_code == 200 and real_path == '/cookie.html':
                cookie_html = "<!DOCTYPE html>\
                        <html>\
                            <head>\
                                <meta charset='utf-8'>\
                                <title>Welcome {}</title>\
                            </head>\
                            <body>\
                                <p> Hello {} </p>\
                                <p> {} seconds left until your cookie expires. </p>\
                            </body>\
                        </html>".format(userid, userid, 30-(float(time.time()) - float(start_time)))
                cookie = open('./src/cookie.html', 'w')
                cookie.write(cookie_html)
                cookie.close()

        if response_code == 200:
            try:
                if real_path == '/cookie.html':
                    content = cookie_html.encode()
                else:
                    f = open('./src{}'.format(real_path), 'rb')
                    content = f.read()
            except:
                response_code = 404

        if response_code == 404:
            f = open('./src/404.html', 'rb')
            content = f.read()
        elif response_code == 403:
            f = open('./src/403.html', 'rb')
            content = f.read()

        content_length = len(content)
        header = self.make_header(content_type, content_length, response_code, msg, user)
        print(header)

        response = header.encode() + content
        client_socket.send(response)

    def make_header(self, content_type, content_length, response_code, msg, user):
        header = ''

        if response_code == 200:
            header += '{} 200 OK\n'.format(msg.split(' ')[2])
        elif response_code == 403:
            header += '{} 403 Forbidden\n'.format(msg.split(' ')[2])
        elif response_code == 404:
            header += '{} 404 NOT FOUND\n'.format(msg.split(' ')[2])
        elif response_code == 301:
            header += '{} 301 Moved Permanently\nLocation: {}\n'.format(msg.split(' ')[2], '/secret.html')

        header += time.strftime("Date: %a, %d %b %Y %H:%M:%S KST\n", time.localtime(time.time()))
        header += "Content-Length: {}\n".format(content_length)
        if content_type == 'html' or response_code == 404 or response_code == 403:
            header += "Content-Type: text/html\n"
        elif content_type == 'None':
            header += "Content-Type: {}\n".format(content_type)
        else:
            header += "Content-Type: image/{}\n".format(content_type)
        if user:
            userid, pw = self.user_parse(user)
            header += "Set-Cookie: id={} time={} valid; Max-Age={}\n".format(userid, time.time(), 30)
        header += "\n"

        return header

    def msg_to_path(self, msg):
        path = msg.split(' ')
        return path

    def parse_path(self, path):
        real_path = path[1]
        user = None
        response_code = 200
        a = False

        if '?' in real_path:
            a = True
            user = real_path.split('?')[1]
            real_path = '/secret.html'

        if real_path == '/':
            real_path = '/index.html'

        try:
            content_type = real_path.split('.')[1]
        except:
            content_type = 'None'
            response_code = 404

        return real_path, response_code, content_type, user, a


    def user_parse(self, user):
        parse_userid = user.split('&')[0]
        parse_pw = user.split('&')[1]
        userid = parse_userid.split('=')[1]
        pw = parse_pw.split('=')[1]
        return userid, pw


def server_handler(client_socket, address):
    start = time.time()
    print('Received from')
    while True:
        try:
            msg = None
            while not msg:
                msg = client_socket.recv(1024)
                time.sleep(0.1)
                if time.time() - start > 10:
                    client_socket.close()
                    return
            if msg:
                link = handler()
                link.response(msg.decode())

        except:
            pass

if __name__ == '__main__':
    port = 10080
    server_socket = socket(AF_INET, SOCK_STREAM)
    host = gethostname()
    print(host)
    server_socket.bind(('', port))
    server_socket.listen(5)

    print('The TCP server is ready to receive.')
    while True:
        client_socket, address = server_socket.accept()
        thread = Thread(target=server_handler, args=(client_socket, address))
        thread.start()
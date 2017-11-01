import os
import file_encription
import BaseHTTPServer
import base64

folder = "folder"
stored_key = "0123456701234567"

def on_receiving_key(key):
    global stored_key
    stored_key = key
    file_encription.decrypt_files(key, folder)

def on_close():
    global stored_key
    file_encription.encrypt_files(stored_key, folder)
    stored_key = ""

def validkey(key):
    return True #TODO

class MYHandler(BaseHTTPServer.BaseHTTPRequestHandler):

    def answer(self, text):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(text)

    def do_GET(self):
        self.do_POST()

    def do_POST(self):
        print "handling GET"

        content_len = int(self.headers.getheader('content-length', 0))
        input = self.rfile.read(content_len)

        if input == "":
            self.answer("Received empty string")

        elif input.startswith("FKEY"):
            key = base64.b64decode(input[4:])
            print "using key", key
            if validkey(key):
                on_receiving_key(key)
            self.answer("PING")

        elif input.startswith("STOP"):
            on_close()
            self.answer("STOP")

        elif input.startswith("PING"):
            self.answer("PING")

        else:
            self.answer("Mesage not recognized")

def run(server_class=BaseHTTPServer.HTTPServer,
        handler_class=MYHandler):
    server_address = ('', 80)
    httpd = server_class(server_address, handler_class)
    httpd.serve_forever()

run()
# DEMO

#on_close() # SETUP

#on_connection() # ->

#on_receiving_key() # <-

#ping() # ->

#on_ping() #<->

#on_close()

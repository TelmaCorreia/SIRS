import os
import file_encription
import BaseHTTPServer
import base64
import core

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
        print "content-lenght", content_len
        incoming = self.rfile.read(content_len)
        print "received", incoming, "with len", len(incoming)
        text = base64.b64decode(incoming)

        message = core.process_raw(text)
        self.answer(base64.b64encode(message))

def run(server_class=BaseHTTPServer.HTTPServer,
        handler_class=MYHandler):
    server_address = ('', 80)
    httpd = server_class(server_address, handler_class)
    httpd.serve_forever()

run()

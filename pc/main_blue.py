import bluetooth
import core
import gui

first_message_size = 256 #in bytes, NOT USING BASE64
fkey_message_size = 64+16
ping_message_size = 48 

class My_Bluetooth_Server():
    is_running = True

    def setFalgFalse(self):
        self.is_running = False

    def create_server(self):
        server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )

        #port = bluetooth.get_available_port( bluetooth.RFCOMM )
        
        #server_sock.bind(("",bluetooth.PORT_ANY))
        port = 1
        server_sock.bind(("",bluetooth.PORT_ANY))
        server_sock.listen(1)
        #print "listening on port %d" % port

        uuid = "1e0ca4ea-299d-4335-93eb-27fcfe7fa848"
        bluetooth.advertise_service( server_sock, "SecureFiles", uuid, service_classes = [uuid, bluetooth.SERIAL_PORT_CLASS ])

        self.server_sock = server_sock

    def receive_message(self, size, client_sock):
        text = client_sock.recv(size)

        print "received [%s]" % text
        try:
            message = core.process_raw(text)
        except Exception as e:
            print e
            return False
        client_sock.send(message)
        return True

    def serve_connection(self, client_sock):
        self.is_running = True
        if not self.receive_message(first_message_size, client_sock) or not self.is_running:
            return
        if not self.receive_message(fkey_message_size, client_sock) or not self.is_running:
            return
        while self.receive_message(ping_message_size, client_sock) and self.is_running:
            pass

    def run_forever(self):
        print "Server started"
        server_sock = self.server_sock
        while True:
            print "Listening for connection"
            client_sock,address = server_sock.accept()
            print "Accepted connection from ",address
            client_sock.settimeout(10)
            print client_sock.gettimeout()

            try:
                self.serve_connection(client_sock)
            except Exception as e:
                print e
                core.locked_closer()
            try:
                client_sock.close()
            except Exception as e:
                print e
            print "Bluetooth Connection closed!!"
        server_sock.close()


print "starting bluetooth server"
server = My_Bluetooth_Server()
core.initialize(server.setFalgFalse)

server.create_server()
server.run_forever()

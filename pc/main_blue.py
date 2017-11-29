import bluetooth
import core
import gui

first_message_size = 256 #in bytes, NOT USING BASE64
fkey_message_size = 64 
ping_message_size = 48 

class My_Bluetooth_Server():
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
        text = client_sock.recv(size) # FIXME Times out? need to break when the smartphone stops sending data

        print "received [%s]" % text
        try:
            message = core.process_raw(text)
        except Exception as e:
            print e
            return False
        client_sock.send(message)
        return True

    def serve_connection(self, client_sock):
        if not self.receive_message(first_message_size, client_sock):
            return
        if not self.receive_message(fkey_message_size, client_sock):
            return
        while self.receive_message(ping_message_size, client_sock):
            pass
            

    def run_forever(self):
        print "Server started. Listening"
        server_sock = self.server_sock
        while True:
            client_sock,address = server_sock.accept()
            print "Accepted connection from ",address

            self.serve_connection(client_sock)

            client_sock.close()
            print "Connection closed!!"
        server_sock.close()


core.initialize()
print "starting bluetooth server"
server = My_Bluetooth_Server()
server.create_server()
server.run_forever()

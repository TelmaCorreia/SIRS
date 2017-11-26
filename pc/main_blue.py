import bluetooth
import core

class My_Bluetooth_Server():
    def create_server(self):
        server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )

        #port = bluetooth.get_available_port( bluetooth.RFCOMM )
        
        server_sock.bind(("",bluetooth.PORT_ANY))
        server_sock.listen(1)
        #print "listening on port %d" % port

        uuid = "1e0ca4ea-299d-4335-93eb-27fcfe7fa848"
        bluetooth.advertise_service( server_sock, "SecureFiles", uuid )

        self.server_sock = server_sock

    def serve_connection(self, client_sock):
        while True:
            text = client_sock.recv(1024) # FIXME Times out? need to break when the smartphone stops sending data
            #FIXME get sizes to receive
            print "received [%s]" % text
            try:
                message = core.process_raw(text)
            except Exception as e:
                print e
                break

            client_sock.send("TEXT")

    def run_forever(self):
        server_sock = self.server_sock
        while True:
            client_sock,address = server_sock.accept()
            print "Accepted connection from ",address

            self.serve_connection(client_sock)

            client_sock.close()
            print "Connection closed!!"

        server_sock.close()


server = My_Bluetooth_Server()
server.create_server()
server.run_forever()


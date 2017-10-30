import os
import file_encription

folder = "folder"
key = "0123456701234567"

def on_connection():
    # TODO ask for key
    print "Asking for key"
    pass

def on_receiving_key():
    for root, dirnames, filenames in os.walk(folder):
        for filename in filenames:
            print "decripting", root+"/"+filename
            file_encription.decrypt_file(key, root+"/"+filename, root+"/"+filename)

            


def on_close():
    for root, dirnames, filenames in os.walk(folder):
        for filename in filenames:
            print "encripting", filename
            file_encription.encrypt_file(key, root+"/"+filename, "tmpfile.tmp")
            # TODO(?) shred plain file?
            os.remove(root+"/"+filename)
            os.rename("tmpfile.tmp", root+"/"+filename)

def ping():
    print "pinging mobile"

def on_ping():
    print "ping received"

    ping()


# DEMO

#on_close() # SETUP

#on_connection() # ->

#on_receiving_key() # <-

#ping() # ->

#on_ping() #<->

#on_close()

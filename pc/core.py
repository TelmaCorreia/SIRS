import file_encription
import my_crypto

# Configuration
folder = "folder"
seconds_until_expiration = 10
key_size = 16

# Session storage
stored_key = "" # FIXME ALL "" by default
session_key = ""
session_iv = ""

def assert_validkey(key):
    if len(key) != key_size:
        raise Exception("Wrong key size", len(key))
    if stored_key != "":
        raise Exception("Folder key already defined")

def on_receiving_key(key):
    global stored_key
    stored_key = key
    file_encription.decrypt_files(key, folder)

def on_close():
    global stored_key
    file_encription.encrypt_files(stored_key, folder)
    stored_key = ""
    # TODO stop timer

def start_session(message):
    if len(message) != key_size*2:
        raise Exception("Wrong session key size, receieved bytes:", len(message))
    global session_iv
    global session_key
    session_key = message[:key_size]
    session_iv = message[key_size:]
    # TODO start timer
    return "OKOK"

def refresh_timer():
    pass

def process_raw(text):
    """Throws exceptions on incorrect messages! Must be handled above"""
    print "RAW:", text
    global session_iv
    global session_key
    if session_key:
        print "CORE: Processing symmetric message"
        message = my_crypto.decompose_message(text, session_key, session_iv)
        print "CORE: Decomposed symmetric message"
        response = process_message(message)
        print "CORE: Processed symmetric message"
        reply =  my_crypto.compose_message(response, session_key, session_iv)
        print "CORE: Composed symmetric message"
        if response == "STOP":
            session_iv = ""
            session_key = ""
        return reply
    else:
        message = my_crypto.decompose_start(text)
        response = start_session(message)
        return my_crypto.compose_start(response, session_key, session_iv)

def process_message(message):
    print "Processing symmetric message:", message
    
    if message == "":
        raise Exception("Received empty string")
    elif message.startswith("FKEY"):
        key = message[4:]
        print "FKEY using key", key, "with len", len(key)
        assert_validkey(key)
        print "FKEY is valid"
        on_receiving_key(key)
        print "Files decrypted"
        return "PING"

    elif message == "STOP":
        print "STOPPING"
        on_close()
        print "Closed"
        return "STOP"

    elif message == "PING":
        refresh_timer()
        print "PING"
        return "PING"
    else:
        raise Exception("Received unkonwn string", message)
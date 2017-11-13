import file_encription
import my_crypto

# Configuration
folder = "folder"
seconds_until_expiration = 10
key_size = 16

# Session storage
stored_key = "0123456701234567" # FIXME ALL "" by default
session_key = "0123456701234567"
session_iv = "0123456701234567"

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
    if session_key:
        message = my_crypto.decompose_message(text, session_key, session_iv)
        response = process_message(message)
        reply =  my_crypto.compose_message(response, session_key, session_iv)
        if response == "STOP":
            global session_iv
            global session_key
            session_iv = ""
            session_key = ""
        return reply
    else:
        message = my_crypto.decompose_start(text)
        response = start_session(message)
        return my_crypto.compose_start(response, session_key, session_iv)

def process_message(message):
    if message == "":
        raise Exception("Received empty string")

    elif message.startswith("FKEY"):
        key = message[4:]
        print "using key", key, "with len", len(key)
        assert_validkey(key)
        on_receiving_key(key)
        return "PING"

    elif message == "STOP":
        on_close()
        return "STOP"

    elif message == "PING":
        refresh_timer()
        return "PING"
    else:
        raise Exception("Received unkonwn string", message)
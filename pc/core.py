import file_encription
import symmetric

# Configuration
folder = "folder"
seconds_until_expiration = 10
key_size = 16

# Session storage
stored_key = "0123456701234567"
stored_iv = "0123456701234567"
session_key = None
session_iv = None

def assert_validkey(key):
    if len(key) != key_size:
        raise Exception("Wrong key size", len(key))

def on_receiving_key(key):
    global stored_key
    stored_key = key
    file_encription.decrypt_files(key, folder)
    # TODO start timer

def on_close():
    global stored_key
    file_encription.encrypt_files(stored_key, folder)
    stored_key = ""
    # TODO stop timer

def process_raw(text):
    """Throws exceptions on incorrect messages! Must be handled above"""
    if session_key:
        message = symmetric.decompose_message(text, session_key, session_iv)
        response = process_message(message)
        return symmetric.compose_message(response, session_key, session_iv)
    else:
        #TODO
        return "TODO, create signed response"


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
        return "PING"

    else:
        raise Exception("Received unkonwn string", message)
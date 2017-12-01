import time
import file_encription
import my_crypto
import gui
import shutdown
from threading import Lock, Timer

# Configuration
folder = "folder"
seconds_until_expiration = 10
key_size = 16

# Session storage
stored_key = "" # FIXME ALL "" by default
session_key = ""
session_iv = ""

lock = Lock()
last_communication = None

def locked_closer(arg = None):
    print "Handling signal, with arg", arg
    with lock:
        on_close()

def initialize():
    gui.show_public()
    shutdown.register_function(locked_closer)


def assert_validkey(key):
    if len(key) != key_size:
        raise Exception("Wrong key size", len(key))
    if stored_key != "":
        raise Exception("Folder key already defined")

def on_receiving_key(old_key, new_key):
    global stored_key
    file_encription.decrypt_files(old_key, folder)
    stored_key = new_key

def on_close():
    global stored_key
    global session_iv
    global session_key
    if stored_key:
        file_encription.encrypt_files(stored_key, folder)
        del stored_key
        stored_key =""
        print "FOLDER KEY DELETED"
    if session_key:
        del session_iv
        session_iv =""
        del session_key
        session_key = ""
        print "SESSION KEY DELETED"
    print "SESSION CLOSED"
    print "="*64


timer_tolerance_seconds = 10
timer_frequency = 20.0

def timer_action():
    with lock:
        print "Timer checking in"
        now = int(time.time())
        if last_communication and abs(now-last_communication) <= timer_tolerance_seconds:
            # All good, start another Timer
            Timer(timer_frequency, timer_action).start()
        else:
            on_close()

def start_session(message):
    if len(message) != key_size*2:
        raise Exception("Wrong session key size, receieved bytes:", len(message))
    global session_iv
    global session_key
    session_key = message[:key_size]
    session_iv = message[key_size:]
    Timer(timer_frequency, timer_action).start()

    return "OKOK"

def refresh_timer():
    pass

def process_raw(text):
    """Throws exceptions on incorrect messages! Must be handled above"""
    with lock:
        try:
            res = process_raw_aux(text)
        except Exception as e:
            on_close()
            raise e
        else:
            global last_communication
            last_communication = int(time.time())
        
    print "\n\n"
    return res
        

def process_raw_aux(text):
    """Throws exceptions on incorrect messages! Must be handled above"""
    print "RAW: size:", len(text), "bytes" #, text
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
        my_crypto.start_session()
        print "CORE: Processing assymetric message"
        message = my_crypto.decompose_start(text)
        print "CORE: Decomposed asymmetric message"
        response = start_session(message)
        print "CORE: Started session"
        reply = my_crypto.compose_message(response, session_key, session_iv)
        print "CORE: Composed signed message"
        return reply

def process_message(message):
    print "Processing symmetric message:", message
    
    if message == "":
        raise Exception("Received empty string")
    elif message.startswith("FKEY"):
        old_key = message[4:key_size+4]
        new_key = message[4+key_size:]
        print "FKEY using key", old_key, "with len", len(old_key)
        assert_validkey(old_key)
        assert_validkey(new_key)
        print "FKEY is valid"
        on_receiving_key(old_key, new_key)
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
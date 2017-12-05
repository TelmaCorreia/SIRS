import time
import file_encription
import my_crypto
import gui
import shutdown
from threading import Lock, Timer

# Configuration
folder = "folder"
key_size = 16

#Configuration
timer_tolerance_seconds = 5
timer_frequency = 5


# Session storage
stored_key = "" # FIXME ALL "" by default
session_key = ""
session_iv = ""

lock = Lock()
last_communication = None
call_on_stop = None

def locked_closer(arg = None):
    print "Handling signal, with arg", arg
    with lock:
        on_close()

def initialize(f = None):
    global call_on_stop
    gui.show_public()
    shutdown.register_function(locked_closer)
    call_on_stop = f


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
    if call_on_stop:
        call_on_stop()
    print "SESSION CLOSED"
    print "="*64

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

    #DEV#print "Sending message with raw bytes:", len(res)
    print ""
    return res


def process_raw_aux(text):
    """Throws exceptions on incorrect messages! Must be handled above"""
    #DEV#print "RAW: size:", len(text), "bytes" #, text
    global session_iv
    global session_key
    if session_key:
        #DEV#print "CORE: Processing symmetric message"
        message = my_crypto.decompose_message(text, session_key, session_iv)
        #DEV#print "CORE: Decomposed symmetric message"
        response = process_message(message)
        #DEV#print "CORE: Processed symmetric message"
        reply =  my_crypto.compose_message(response, session_key, session_iv)
        #DEV#print "CORE: Composed symmetric message"
        if response == "STOP":
            on_close()
            print "Closed"
        return reply
    else:
        my_crypto.start_session()
        #DEV#print "CORE: Processing assymetric message"
        message = my_crypto.decompose_start(text)
        #DEV#print "CORE: Decomposed asymmetric message"
        response = start_session(message)
        #DEV#print "CORE: Started session"
        reply = my_crypto.compose_message(response, session_key, session_iv)
        #DEV#print "CORE: Composed hashed message"
        return reply

def process_message(message):
    #DEV#print "Processing symmetric message:", message
    #DEV#print "len is ", len(message)
    
    if message == "":
        raise Exception("Received empty string")
    elif message.startswith("FKEY"):
        old_key = message[4:key_size+4]
        new_key = message[4+key_size:]
        #DEV#print "FKEY using old key", old_key, "with len", len(old_key)
        #DEV#print "FKEY using new key", new_key, "with len", len(new_key)
        assert_validkey(old_key)
        assert_validkey(new_key)
        #DEV#print "FKEY is valid"
        on_receiving_key(old_key, new_key)
        print "Files decrypted"
        return "PING"

    elif message == "STOP":
        print "STOPPING"
        #on_close()
        return "STOP"

    elif message == "PING":
        refresh_timer()
        print "PING"
        return "PING"
    else:
        raise Exception("Received unkonwn string", message)
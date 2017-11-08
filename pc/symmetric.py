from Crypto.Cipher import AES
from  Crypto.Random import random
import struct
import time
from Crypto.Hash import SHA256
import base64

def hash_text(text):
    hash = SHA256.new()
    hash.update(text)
    return hash.digest()

######################################### FRESHNESS #########################################
tolerance_seconds = 3
timestamp_format = "I"

def is_fresh(timestamp):
    now = int(time.time())

    return abs(now-timestamp) <= tolerance_seconds

def time_message(message):
    timestamp = int(time.time())
    nonced_message = struct.pack('<'+timestamp_format, timestamp) + message
    return nonced_message

def hash_message(message):
    return hash_text(message) + message

def check_hash_message(text):
    hash_size = 32
    if len(text) < hash_size:
        raise Exception("Hash", "Message too small")

    received_hash = text[:hash_size]

    actual_hash = hash_text(text[hash_size:])

    if received_hash != actual_hash:
        raise Exception("Hash", "Mismatching Hash!")

    return text[hash_size:]

def check_time_message(text):
    timestamp_size = struct.calcsize(timestamp_format)
    timestamp_text = text[:timestamp_size]
    received_timestamp = struct.unpack('<'+timestamp_format, timestamp_text)[0]
    if not is_fresh(received_timestamp):
        raise Exception("Freshness", "Old message received")

    message = text[timestamp_size:]
    return message

######################################### Encryption #########################################
def encrypt_string(message="", key="0123456701234567", iv="0123456701234567"):
    """Returns a string with encrypted message(AES CBC). adds padding as needed"""
    # secure random iv
    #iv = ''.join(chr(random.randint(0, 0xFF)) for i in range(16))
    message_size = len(message)
    encryptor = AES.new(key, AES.MODE_CBC, iv)
    # padding
    padding_size = 16 - len(message) % 16
    message += chr(padding_size) * padding_size

    return encryptor.encrypt(message)

def decrypt_string(encrypted_text, key="0123456701234567", iv="0123456701234567"):
    """
        Expects a string with messagesize+iv+encrypted message(AES CBC)
        Will return extra butes at the end, corresponding to the padding. They are to be ignored by the upper layer
    """
    decryptor = AES.new(key, AES.MODE_CBC, iv)
    padded_message = decryptor.decrypt(encrypted_text)

    padding_size = ord(padded_message[-1])
    message = padded_message[:-padding_size]
    return message

######################################### Stack #########################################
def compose_message(message, key="0123456701234567", iv="0123456701234567"):
    timed = time_message(message)
    hashed = hash_message(timed)
    encrypted = encrypt_string(hashed, key, iv)
    return encrypted
    #return base64.b64encode(encrypted)

def decompose_message(text, key="0123456701234567", iv="0123456701234567"):
    #raw = base64.b64decode(text)
    decrypted = decrypt_string(text, key, iv)
    correct_message = check_hash_message(decrypted)
    fresh_message = check_time_message(correct_message)
    return fresh_message


#print encrypt_string("A")
#print decrypt_string(encrypt_string("A"))


#composed =  compose_message("A")
#print composed
#print decompose_message(composed)


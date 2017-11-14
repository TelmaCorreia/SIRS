import struct
import time
from Crypto.Cipher import AES
from Crypto.PublicKey import RSA
from Crypto.Hash import SHA256
from Crypto.Random import random
from Crypto import Signature
from Crypto import Cipher

rsa_key_file = "../private_key.pem"

def hash_text(text):
    """SHA256 hash"""
    hash = SHA256.new()
    hash.update(text)
    return hash.digest()

######################################### FRESHNESS #########################################
tolerance_seconds = 3
timestamp_format = "I"

def is_fresh(timestamp):
    """Checks if timestamp is within the tolerance"""
    now = int(time.time())
    return abs(now-timestamp) <= tolerance_seconds

def time_message(message):
    """Returns timestamp+message"""
    timestamp = int(time.time())
    nonced_message = struct.pack('<'+timestamp_format, timestamp) + message
    return nonced_message

def check_time_message(text):
    """Assumes timestamp+message. Checks if the timestamp is fresh"""
    timestamp_size = struct.calcsize(timestamp_format)
    timestamp_text = text[:timestamp_size]
    received_timestamp = struct.unpack('<'+timestamp_format, timestamp_text)[0]
    if not is_fresh(received_timestamp):
        raise Exception("Freshness", "Old message received")

    message = text[timestamp_size:]
    return message

######################################### INTEGRITY #########################################
def hash_message(message):
    """Returns hash+message"""
    return hash_text(message) + message

def sign_message(message):
    """Returns signed_hash+message"""
    rsa_key = RSA.importKey(open(rsa_key_file, "rb").read())
    signer = Signature.PKCS1_v1_5.new(rsa_key)
    signature = signer.sign(message)
    #message_hash = hash_text(message)
    #signature = rsa_key.decrypt(message_hash) # TODO switch to recommended signature. proper padding
    return signature + message

def check_hash_message(text):
    """Assumes text is "hash+message". checls if message corresponds to the given hash."""
    hash_size = 32
    if len(text) < hash_size:
        raise Exception("Hash", "Message too small")

    received_hash = text[:hash_size]
    actual_hash = hash_text(text[hash_size:])

    if received_hash != actual_hash:
        raise Exception("Hash", "Mismatching Hash!")

    return text[hash_size:]
######################################### Encryption #########################################
def encrypt_string(message="", key="0123456701234567", iv="0123456701234567"):
    """Returns a string with encrypted message(AES CBC). adds padding as needed"""
    encryptor = AES.new(key, AES.MODE_CBC, iv)
    
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

def decompose_message(text, key="0123456701234567", iv="0123456701234567"):
    decrypted = decrypt_string(text, key, iv)
    correct_message = check_hash_message(decrypted)
    fresh_message = check_time_message(correct_message)
    return fresh_message

def decompose_start(message):
    rsa_key = RSA.importKey(open(rsa_key_file, "rb").read())
    #decrypted =  rsa_key.decrypt(message) # TODO swith to recommended. proper padding
    decryptor = Cipher.PKCS1_v1_5.new(rsa_key)
    decrypted = decryptor.decrypt(message, "ERROR")

    #decrypted = message # TODO RSA
    correct_message = check_hash_message(decrypted)
    fresh_message = check_time_message(correct_message)
    return fresh_message

def compose_start(message, key, iv):
    timed = time_message(message)
    hashed = sign_message(timed)
    encrypted = encrypt_string(hashed, key, iv)
    return encrypted

#print encrypt_string("A")
#print decrypt_string(encrypt_string("A"))


#composed =  compose_message("A")
#print composed
#print decompose_message(composed)


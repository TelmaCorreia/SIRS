from Crypto.Cipher import AES
from  Crypto.Random import random
import struct

def encrypt_string(message="", key="0123456701234567"):
    """Returns a string with messagesize+iv+encrypted message(AES CBC)"""
    # secure random iv
    iv = ''.join(chr(random.randint(0, 0xFF)) for i in range(16))
    message_size = len(message)
    encryptor = AES.new(key, AES.MODE_CBC, iv)
    # padding
    message += ''.join(chr(random.randint(0, 0xFF)) for i in range(16 - len(message) % 16))

    return struct.pack('<Q', message_size) + iv + encryptor.encrypt(message)

def decrypt_string(encrypted_text, key="0123456701234567"):
    """Expects a string with messagesize+iv+encrypted message(AES CBC)"""
    message_size_size = struct.calcsize('Q')
    iv_size = 16

    origsize = struct.unpack('<Q', encrypted_text[0:message_size_size])[0]
    iv = encrypted_text[message_size_size:message_size_size+iv_size]

    encrpyted_message = encrypted_text[(message_size_size + iv_size):]
    decryptor = AES.new(key, AES.MODE_CBC, iv)

    return decryptor.decrypt(encrpyted_message)[:origsize]

print encrypt_string("A")

print decrypt_string(encrypt_string("A"))
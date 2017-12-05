import os, struct
import time
from Crypto.Cipher import AES
from Crypto.Random import random

integrity_verifier = "EVERYTHINGOK_THIS_STRING_IS_KNOWN_bUT KINDA big12345678901234567"
max_padding_size = 16
verifier_size = len(integrity_verifier)
tail_size = max_padding_size + verifier_size

def secure_delete(path):
    with open(path, "wb+") as f:
        length = f.tell()
        f.seek(0)
        f.write(os.urandom(length))
    os.remove(path)

def encrypt_file(key, in_filename, out_filename=None, chunksize=64*1024):
    """ Encrypts a file using AES (CBC mode) with the
        given key.

        key:
            The encryption key - a string that must be
            either 16, 24 or 32 bytes long. Longer keys
            are more secure.

        in_filename:
            Name of the input file

        out_filename:
            If None, '<in_filename>.enc' will be used.

        chunksize:
            Sets the size of the chunk which the function
            uses to read and encrypt the file. Larger chunk
            sizes can be faster for some files and machines.
            chunksize must be divisible by 16.
    """
    if not out_filename:
        out_filename = in_filename + '.enc'

    iv = ''.join(chr(random.randint(0, 0xFF)) for i in range(16))
    encryptor = AES.new(key, AES.MODE_CBC, iv)
    filesize = os.path.getsize(in_filename)

    with open(in_filename, 'rb') as infile:
        with open(out_filename, 'wb') as outfile:
            outfile.write(struct.pack('<Q', filesize))
            outfile.write(iv)

            chunk = infile.read(chunksize)
            file_done = False
            while True:
                if len(chunk) != chunksize: #we read the last of the file
                    chunk += integrity_verifier
                    #chunk += ''.join(chr(random.randint(0, 0xFF)) for i in range(max_padding_size - len(chunk) % max_padding_size))
                    padding_size = max_padding_size - len(chunk) % max_padding_size
                    chunk += ''.join(chr(padding_size) for i in range(padding_size))
                    file_done = True

                
                outfile.write(encryptor.encrypt(chunk))
                if file_done:
                    break
                else:
                    chunk = infile.read(chunksize)
                

def decrypt_file(key, in_filename, out_filename=None, chunksize=24*1024):
    """ Decrypts a file using AES (CBC mode) with the
        given key. Parameters are similar to encrypt_file,
        with one difference: out_filename, if not supplied
        will be in_filename without its last extension
        (i.e. if in_filename is 'aaa.zip.enc' then
        out_filename will be 'aaa.zip')
    """
    if not out_filename:
        out_filename = os.path.splitext(in_filename)[0]

    with open(in_filename, 'rb') as infile:
        origsize = struct.unpack('<Q', infile.read(struct.calcsize('Q')))[0]
        iv = infile.read(16)
        decryptor = AES.new(key, AES.MODE_CBC, iv)
        tail = ""

        with open(out_filename, 'wb') as outfile:
            while True:
                chunk = infile.read(chunksize)

                if len(chunk) == 0:
                    break
                plain = decryptor.decrypt(chunk)
                if len(plain) >= tail_size:
                    tail = plain[-tail_size:]
                else:
                    tail = (tail+plain)[-tail_size:]

                outfile.write(plain)

            outfile.truncate(origsize)
            if verifier_size > len(tail):
                raise Exception("Incorrect file key")
            padding_size = ord(tail[-1])
            verifier = tail[:-padding_size][-verifier_size:]
            if verifier != integrity_verifier:
                raise Exception("Incorrect file key")



def decrypt_files(key, folder):
    start = time.time()
    plain_files = []

    try:
        for root, dirnames, filenames in os.walk(folder):
            for filename in filenames:
                print "decripting", root+"/"+filename
                try:
                    decrypt_file(key, root+"/"+filename, "tmpfile.tmp")
                except Exception as e:
                    try:
                        #os.remove("tmpfile.tmp")
                        secure_delete("tmpfile.tmp")
                    except Exception as e_rm:
                        pass
                    raise e
                os.remove(root+"/"+filename)
                os.rename("tmpfile.tmp", root+"/"+filename)
                plain_files.append(root+"/"+filename)
    except Exception as e:
        try:
            #os.remove("tmpfile.tmp")
            secure_delete("tmpfile.tmp")
        except Exception as e_rm:
            pass

        for file in plain_files:
            encrypt_file_using_tmp(key, file)
        raise e
    end = time.time()
    print "decrypted in ", end-start

def encrypt_file_using_tmp(key, file):
    encrypt_file(key, file, "tmpfile.tmp")
    secure_delete(file)
    #os.remove(file)
    os.rename("tmpfile.tmp", file)


def encrypt_files(key, folder):
    start = time.time()
    for root, dirnames, filenames in os.walk(folder):
        for filename in filenames:
            print "encripting", filename
            file = root+"/"+filename
            encrypt_file_using_tmp(key, file)
    end = time.time()
    print "encrypted in ", end-start
            


if __name__ == "__main__":
    encrypt_file("0123456701234567", "teste.txt", "teste.enc")
    decrypt_file("0123456701234567", "teste.enc", "teste.dec.txt")
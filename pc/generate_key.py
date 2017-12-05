from os import chmod
from Crypto.PublicKey import RSA


rsa_key_file = "../private_key.der"
rsa_public_key_file = "../public_key.der"

######################################### INTEGRITY #########################################

def save_key(key):
    with open(rsa_key_file, "wb") as f:
        chmod(rsa_key_file, 0600)
        f.write(key.exportKey(format='DER'))
    with open(rsa_public_key_file, "wb") as f:
        f.write(key.publickey().exportKey(format='DER'))

def save_new_key():
    key = RSA.generate(2048)
    save_key(key)

if __name__ == "__main__":
    save_new_key()

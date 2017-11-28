import base64
import qrcode

rsa_public_key_file = "..\public_key.der"

def show_public():
    text = base64.b64encode(open(rsa_public_key_file, "rb").read())
    img = qrcode.make(text)
    img.show()
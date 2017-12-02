package com.sirs.smartcipher;

import android.util.Base64;

import com.sirs.smartcipher.security.SecurityHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.util.Base64.NO_WRAP;

public class Core {

    private SecurityHelper sh;

    public Core() throws CertificateException, NoSuchAlgorithmException,
            KeyStoreException, UnrecoverableEntryException, NoSuchProviderException,
            InvalidAlgorithmParameterException, IOException {

        this.sh= new SecurityHelper();
    }

    public SecurityHelper getSecurityHelper(){
        return sh;
    }

    public String generateMessage(String requestType) throws NoSuchPaddingException,
            NoSuchAlgorithmException, KeyStoreException, IOException, BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException,
            CertificateException, UnrecoverableEntryException, NoSuchProviderException,
            InvalidAlgorithmParameterException {

        switch(requestType){
            case Constants.START_CONNECTION:
                return startConnection(requestType);
            case Constants.FKEY:
                return sendFKEY(requestType);
            case Constants.PING:
                return sendSimpleMessage(requestType);
            case Constants.STOP:
                return sendSimpleMessage(requestType);
            default:
                return "ERROR";
        }

    }

    //process responses from server
    public boolean processResponse(String requestType, String response) throws
            NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, SignatureException, KeyStoreException, IOException {

        //decrypt message
        byte[] decryptedResponse = getSecurityHelper().decrypt(Base64.decode(response.getBytes(), NO_WRAP));
        //decompose message
        HashMap<String, byte[]> map = getSecurityHelper().decomposeMsgSymmetricEncryption(decryptedResponse);

        return validateResponse(map);

    }

    //validate response from server
    private boolean validateResponse(HashMap<String, byte[]> map) {

        boolean nonce = getSecurityHelper().checkValidNonce(map.get("nonce"));
        boolean counter = getSecurityHelper().checkValidCounter(map.get("counter"));
        byte [] myhash = getSecurityHelper().messageDigest(map.get("data"));
        boolean hash = getSecurityHelper().checkHash(map.get("hash"), myhash);

        return (nonce && hash && counter);

    }

    //asymmetric message that contains the session key and iv
    private String startConnection(String requestType) throws NoSuchPaddingException,
            InvalidKeySpecException, NoSuchAlgorithmException, KeyStoreException,
            BadPaddingException, IllegalBlockSizeException, InvalidKeyException, IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.write(getSecurityHelper().getSessionKey());
        outputStream.write(getSecurityHelper().getIVSK());

        byte[] content = getSecurityHelper().composeMsgAsymetricEncryption(outputStream.toByteArray());
        return Base64.encodeToString(content, NO_WRAP);
    }

    //symmetric message that contains the encryption file key (every new session a new file key is generated)
    private String sendFKEY(String requestType) throws IOException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException,
            NoSuchProviderException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            InvalidKeyException, BadPaddingException, NoSuchPaddingException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.write(requestType.getBytes());
        outputStream.write(getSecurityHelper().getOldFileKey());
        outputStream.write(getSecurityHelper().getFileKey());

        byte[] content = getSecurityHelper().composeMsgSymetricEncryption(outputStream.toByteArray());

        return  Base64.encodeToString(content, NO_WRAP);

    }

    //used to pings and stop messages
    private String sendSimpleMessage(String requestType) throws NoSuchPaddingException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException, IOException {

        byte[] content = getSecurityHelper().composeMsgSymetricEncryption(requestType.getBytes());

        return   Base64.encodeToString(content, NO_WRAP);

    }


}

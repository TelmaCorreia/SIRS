package com.sirs.smartcipher;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;


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

import static android.content.ContentValues.TAG;
import static android.util.Base64.DEFAULT;
import static android.util.Base64.NO_PADDING;
import static android.util.Base64.NO_WRAP;

/**
 * Created by telma on 11/21/2017.
 */

public class RequestManager {

    public static final String START_CONNECTION ="";
    public static final String PING = "PING";
    public static final String FKEY = "FKEY";
    public static final String STOP = "STOP";
    private SecurityHelper sh;

    public RequestManager() throws CertificateException, NoSuchAlgorithmException,
            KeyStoreException, UnrecoverableEntryException, NoSuchProviderException,
            InvalidAlgorithmParameterException, IOException {

        this.sh= new SecurityHelper();
    }

    public SecurityHelper getSecurityHelper(){
        return sh;
    }


    public String startConnection(String requestType) throws NoSuchPaddingException,
            InvalidKeySpecException, NoSuchAlgorithmException, KeyStoreException,
            BadPaddingException, IllegalBlockSizeException, InvalidKeyException, IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(getSecurityHelper().getSessionKey());
            outputStream.write(getSecurityHelper().getIVSK());
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] content = getSecurityHelper().composeMsgAsymetricEncryption(outputStream.toByteArray());
        return Base64.encodeToString(content, NO_WRAP);
    }
    public String sendFKEY(String requestType) throws IOException, CertificateException,
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

    public String sendSimpleMessage(String requestType) throws NoSuchPaddingException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException, IOException {

        byte[] content = getSecurityHelper().composeMsgSymetricEncryption(requestType.getBytes());
        return   Base64.encodeToString(content, NO_WRAP);
    }

    public String generateMessage(String requestType) throws NoSuchPaddingException,
            NoSuchAlgorithmException, KeyStoreException, IOException, BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException,
            CertificateException, UnrecoverableEntryException, NoSuchProviderException,
            InvalidAlgorithmParameterException {

        switch(requestType){
            case START_CONNECTION:
                return startConnection(requestType);
            case FKEY:
                return sendFKEY(requestType);
            case PING:
                return sendSimpleMessage(requestType);
            case STOP:
                return sendSimpleMessage(requestType);
            default:
                return "ERROR";
        }

    }

    public boolean processResponse(String requestType, String response) throws
            NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, SignatureException, KeyStoreException, IOException {

        //decrypt message
        byte[] decryptedResponse = getSecurityHelper().decrypt(Base64.decode(response.getBytes(), NO_WRAP));
        //decompose message
        if (requestType.equals(START_CONNECTION)){
            HashMap<String, byte[]> map = getSecurityHelper().decomposeMsgSymmetricEncryption(decryptedResponse);
            return validateResponse(map);
            //return validateResponseStart(map);
        }else{
            HashMap<String, byte[]> map = getSecurityHelper().decomposeMsgSymmetricEncryption(decryptedResponse);

            return validateResponse(map);

        }

    }

    private boolean validateResponse(HashMap<String, byte[]> map) {

        boolean nonce = getSecurityHelper().checkValidNonce(map.get("nonce"));
        boolean counter = getSecurityHelper().checkValidCounter(map.get("counter"));
        byte [] myhash = getSecurityHelper().messageDigest(map.get("data"));
        boolean hash = getSecurityHelper().checkHash(map.get("hash"), myhash);

        return (nonce && hash && counter);

    }

    private boolean validateResponseStart(HashMap<String, byte[]> map) throws
            NoSuchAlgorithmException, KeyStoreException, IOException, SignatureException,
            InvalidKeyException, InvalidKeySpecException {

        boolean nonce = getSecurityHelper().checkValidNonce(map.get("nonce"));
        boolean counter = getSecurityHelper().checkValidCounter(map.get("counter"));
        boolean hash  = getSecurityHelper().verifySignature(map.get("hash"), map.get("data"));

        return (nonce && hash && counter);

    }



}

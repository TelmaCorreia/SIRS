package com.sirs.smartcipher.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.sirs.smartcipher.Constants;
import com.sirs.smartcipher.MyApp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by telma on 11/19/2017.
 */

public class SecurityHelper {

    private static final String TAG = "SecurityHelper";
    private byte[] fileKey;
    private byte[] sessionKey;
    private byte[] oldFileKey;
    private byte[] initializationVectorSK;
    private int counter;
    private KeyStore mStore;

    public SecurityHelper() throws CertificateException, NoSuchAlgorithmException,
            KeyStoreException, IOException, InvalidAlgorithmParameterException,
            UnrecoverableEntryException, NoSuchProviderException {

        this.fileKey = getFileKey();//fixme
        this.sessionKey = generateRandom();
        this.initializationVectorSK = generateRandom();
        this.counter = 0;

    }


    public byte[] getFileKey() throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException,
            IOException, UnrecoverableEntryException {

        Context context = MyApp.getAppContext();
        SharedPreferences sharedPref = context.getSharedPreferences("PK_FILE", Context.MODE_PRIVATE);
        String filekey = sharedPref.getString("FILEKEY", "");
        if (!sharedPref.contains("FILEKEY")){
            byte[] fk= generateRandom();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("FILEKEY", String.valueOf(fk));
            editor.commit();
            oldFileKey = fk;
            return fk;
        }else{
            oldFileKey = filekey.getBytes();
            return generateRandom();
        }

    }

    public byte[] getOldFileKey(){
        return oldFileKey;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public byte[] getOldSessionKey() {
        return sessionKey;
    }


    public byte[] getIVSK() {
        return initializationVectorSK;
    }

    public int getCounter() {
        return counter;
    }

    public void incrementCounter(int count) {
        this.counter = count + 1;
    }

    private PublicKey getPublicKey() throws NoSuchAlgorithmException, KeyStoreException,
            IOException, InvalidKeySpecException {
        byte[] keyBytes = readFromfile(Constants.FILENAME, MyApp.getAppContext());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pk = kf.generatePublic(spec);

        return pk;

    }

    /** Use only if you are on a real device **/
//    private PublicKey getPublicKey() throws NoSuchAlgorithmException, KeyStoreException,
//            IOException, InvalidKeySpecException {
//
//        //SAVE PK IF DOES NOT EXIST
//        Context context = MyApp.getAppContext();
//        SharedPreferences sharedPref = context.getSharedPreferences("PK_FILE", Context.MODE_PRIVATE);
//        String pubkey = sharedPref.getString("PK", "");
//        if (!sharedPref.contains("PK") ){
//            Log.d("PK", "No publick key associated");
//            return null;
//        }else {
//            byte[] keyBytes = pubkey.getBytes();
//            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
//            KeyFactory kf = KeyFactory.getInstance("RSA");
//            PublicKey pk = kf.generatePublic(spec);
//            return pk;
//        }
//
//    }


    public byte[] readFromfile(String fileName, Context context) {
        try {
            InputStream is = context.getResources().getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size]; //declare the size of the byte array with size of the file
            is.read(buffer); //read file
            is.close(); //close file

            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**********************************************************************************************
     * Asymmetric Functions
     **********************************************************************************************/

    //Asymmetric encryption
    public byte[] encryptAsymmetric(byte[] inputData) throws InvalidKeySpecException,
            NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchPaddingException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        PublicKey key = getPublicKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.PUBLIC_KEY, key);
        byte[] encryptedBytes = cipher.doFinal(inputData);

        return encryptedBytes;
    }

    //Compose Asymmetric message: (content of the message) -> hash + nonce + msg
    public byte[] composeMsgAsymetricEncryption(byte[] content) throws NoSuchPaddingException,
            NoSuchAlgorithmException, KeyStoreException, IOException, BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException {

        byte[] nonce = generateNonce();
        byte[] counter = ByteBuffer.allocate(4).putInt(getCounter()).array();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(nonce);
            outputStream.write(counter);
            outputStream.write(content);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        byte data[] = outputStream.toByteArray();
        System.out.println("counter sent: " + counter);

        byte[] hash = messageDigest(data);
        outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(hash);
            outputStream.write(data);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return encryptAsymmetric(outputStream.toByteArray());
    }


    public HashMap<String, byte[]> decomposeMsgAsymmetricEncryption(byte[] content) {
        HashMap<String, byte[]> map = new HashMap();
        int hash_lenght = 32;
        map.put("hash", Arrays.copyOfRange(content, 0, hash_lenght));
        map.put("nonce", Arrays.copyOfRange(content, hash_lenght, hash_lenght + 4));
        map.put("counter", Arrays.copyOfRange(content, hash_lenght +4, hash_lenght +4 + 4));
        map.put("data", Arrays.copyOfRange(content, hash_lenght+4, content.length));


        return map;
    }

    /**********************************************************************************************
     * Symmetric Functions
     **********************************************************************************************/

    //Symmetric Encryption
    public byte[] encrypt(byte[] value) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException {

        IvParameterSpec initializationVector = new IvParameterSpec(getIVSK());
        Log.d("teste", String.valueOf(getSessionKey()));
        Log.d("teste", String.valueOf(getIVSK()));


        SecretKeySpec secretKeySpec = new SecretKeySpec(getSessionKey(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, initializationVector);
        byte[] encrypted = cipher.doFinal(value);

        return encrypted;

    }

    //Symmetric Decrypt
    public byte[] decrypt(byte[] encrypted) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException {

        IvParameterSpec initializationVector = new IvParameterSpec(getIVSK());
        SecretKeySpec secretKeySpec = new SecretKeySpec(getSessionKey(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, initializationVector);
        byte[] original = cipher.doFinal(encrypted);

        return original;

    }

    //Compose Symmetric message: (content of the message) -> hash + nonce + msg
    public byte[] composeMsgSymetricEncryption(byte[] content) throws NoSuchPaddingException,
            InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, IOException {

        byte[] nonce = generateNonce();
        byte[] counter = ByteBuffer.allocate(4).putInt(getCounter()).array();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(nonce);
            outputStream.write(counter);
            outputStream.write(content);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        byte data[] = outputStream.toByteArray();

        byte[] hash = messageDigest(data);
        outputStream = new ByteArrayOutputStream();
        outputStream.write(hash);
        outputStream.write(data);

        return encrypt(outputStream.toByteArray());
    }

    // Decompose Symmetric message: message decrypted -> hash, nonce, msg
    public HashMap<String, byte[]> decomposeMsgSymmetricEncryption(byte[] content) {
        HashMap<String, byte[]> map = new HashMap();
        map.put("hash", Arrays.copyOfRange(content, 0, 32));
        map.put("nonce", Arrays.copyOfRange(content, 32, 32 + 4));
        map.put("counter", Arrays.copyOfRange(content, 36, 36 + 4));
        map.put("data", Arrays.copyOfRange(content, 32, content.length));


        return map;
    }

    /**********************************************************************************************
     * Auxiliary functions
     **********************************************************************************************/

    //used for sessionkey and sessionIV
    private byte[] generateRandom() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[Constants.LENGHT];
        random.nextBytes(bytes);
        return bytes;
    }

    //get timestamp and transform to byte[]
    public byte[] generateNonce() {
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        System.out.println("nonce: " + timestamp);
        ByteBuffer nonce = ByteBuffer.allocate(Integer.BYTES);
        nonce.putInt(timestamp);
        return nonce.array();
    }

    //validate if message is fresh
    public boolean checkValidNonce(byte[] nonce) {
        ByteBuffer bb = ByteBuffer.wrap(nonce);
        int timestamp = bb.getInt();
        int now = (int) (System.currentTimeMillis() / 1000);
        boolean valid = Math.abs(now - timestamp) <= Constants.TOLERANCE;
        System.out.println("Valid = " + valid + " ts = " + timestamp + " now = " + now);
        return valid;
    }

    //validate message integrity
    public boolean checkHash(byte[] hash, byte[] myHash) {
        boolean valid = Arrays.equals(hash, myHash);
        System.out.println("Valid = " + valid);
        return valid;

    }

    public boolean checkValidCounter(byte[] counter) {

        ByteBuffer bb = ByteBuffer.wrap(counter);
        int count = bb.getInt();
        int prevCounter = getCounter();
        boolean valid = false;
        if ((count - 1) == prevCounter) {
            valid = true;
            incrementCounter(count);
        }
        return valid;
    }

    public boolean verifySignature(byte[] signature, byte[] data) throws NoSuchAlgorithmException,
            InvalidKeySpecException, KeyStoreException, IOException, InvalidKeyException,
            SignatureException {
        Signature signature1;

        signature1 = Signature.getInstance("SHA256withRSA");
        signature1.initVerify(getPublicKey());
        signature1.update(data);
        boolean result = signature1.verify(signature);
        return result;

    }


    public byte[] messageDigest(byte[] data) {

        byte[] dataDigest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            MessageDigest dataMD = (java.security.MessageDigest) md.clone();
            dataDigest = dataMD.digest();
        } catch (CloneNotSupportedException cnse) {
            Log.e(TAG, "couldn't make digest of partial content");
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, e.getMessage());
        }

        return dataDigest;

    }


}

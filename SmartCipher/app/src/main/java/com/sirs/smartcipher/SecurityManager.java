package com.sirs.smartcipher;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
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
import java.util.Calendar;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import java.security.KeyStore;

/**
 * Created by telma on 11/19/2017.
 */

public class SecurityManager {

    private static final String TAG = "SecurityManager" ;
    private byte[] fileKey;
    private byte[] sessionKey;
    private byte[] initializationVectorSK;
    private int counter;
    private KeyStore mStore;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public SecurityManager() throws CertificateException, NoSuchAlgorithmException,
            KeyStoreException, IOException, InvalidAlgorithmParameterException,
            UnrecoverableEntryException, NoSuchProviderException {

        loadKeyStore();
        this.fileKey=getFileKey();
        this.sessionKey = generateRandom();
        this.initializationVectorSK = generateRandom();
        this.counter=0;

    }

    void loadKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException,
            IOException {
        mStore = KeyStore.getInstance(Constants.KEYSTORE_PROVIDER);
        mStore.load(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public byte[] getFileKey( ) throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException,
            IOException, UnrecoverableEntryException {

        if(!mStore.containsAlias(Constants.AES_KEY_ALIAS)){
            return generateAESKey();
        }else{
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry)
                    mStore.getEntry(Constants.AES_KEY_ALIAS, null);
            return entry.getSecretKey().getEncoded();
        }
    }

    public byte[] getSessionKey(){
        return sessionKey;
    }

    public byte[] getIVSK(){
        return initializationVectorSK;
    }

    public int getCounter(){
        return counter;
    }

    public void incrementCounter(int count){
        this.counter= count+1;
    }

    //FIXME
    @RequiresApi(api = Build.VERSION_CODES.O)
    private PublicKey getPublicKey() throws NoSuchAlgorithmException, KeyStoreException,
            IOException, InvalidKeySpecException {

        //FIXME: it works?
        byte[] keyBytes = Files.readAllBytes(Paths.get(Constants.FILENAME));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pk = kf.generatePublic(spec);
        return pk;

    }


    /**********************************************************************************************
    * Asymmetric Functions
    **********************************************************************************************/

    //Asymmetric encryption
    @RequiresApi(api = Build.VERSION_CODES.O)
    public byte[] encryptAsymmetric(byte[] inputData) throws InvalidKeySpecException,
            NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchPaddingException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        PublicKey key= getPublicKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.PUBLIC_KEY, key);
        byte[] encryptedBytes = cipher.doFinal(inputData);

        return encryptedBytes;
    }

    //Asymmetric decryption
    @RequiresApi(api = Build.VERSION_CODES.O)
    public byte[] decryptAsymmetric(byte[] inputData) throws InvalidKeySpecException,
            KeyStoreException, IOException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        PublicKey key= getPublicKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] original= cipher.doFinal(inputData);

        return original;

    }

    //Compose Asymmetric message: (content of the message) -> hash + nonce + msg
    @RequiresApi(api = Build.VERSION_CODES.O)
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
        byte data[] = outputStream.toByteArray( );
        System.out.println("counter sent: "+ counter);

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
        map.put("hash", Arrays.copyOfRange(content, 0, 256));
        map.put("nonce", Arrays.copyOfRange(content, 256, 256+4));
        map.put("counter", Arrays.copyOfRange(content, 260, 260+4));


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
            BadPaddingException, InvalidAlgorithmParameterException {

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
        byte data[] = outputStream.toByteArray( );

        byte[] hash = messageDigest(data);
        outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(hash);
            outputStream.write(data);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return encrypt(outputStream.toByteArray());
    }

    // Decompose Symmetric message: message decrypted -> hash, nonce, msg
    public HashMap<String, byte[]> decomposeMsgSymmetricEncryption(byte[] content) {
        HashMap<String, byte[]> map = new HashMap();
        map.put("hash", Arrays.copyOfRange(content, 0, 32));
        map.put("nonce", Arrays.copyOfRange(content, 32, 32+4));
        map.put("counter", Arrays.copyOfRange(content, 36, 36+4));
        map.put("data",Arrays.copyOfRange(content, 32, content.length));


        return map;
    }

    /**********************************************************************************************
     * Auxiliary functions
     **********************************************************************************************/

    //used for sessionkey and sessionIV
    private byte[] generateRandom(){
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[Constants.LENGHT];
        random.nextBytes(bytes);
        return bytes;
    }

    //get timestamp and transform to byte[]
    public byte[] generateNonce(){
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        System.out.println("nonce: " +timestamp);
        ByteBuffer nonce = ByteBuffer.allocate(Integer.BYTES);
        nonce.putInt(timestamp);
        return nonce.array();
    }

    //validate if message is fresh
    public boolean checkValidNonce(byte[] nonce){
        ByteBuffer bb = ByteBuffer.wrap(nonce);
        int timestamp = bb.getInt();
        int now = (int) (System.currentTimeMillis() / 1000);
        boolean valid =  Math.abs(now-timestamp)<= Constants.TOLERANCE;
        System.out.println("Valid = " + valid + " ts = "+timestamp + " now = " +now);
        return valid;
    }

    //validate message integrity
    public boolean checkHash(byte[] hash, byte[] myHash){
        boolean valid = Arrays.equals(hash, myHash);
        System.out.println("Valid = " + valid);
        return valid;

    }

    public boolean checkValidCounter(byte[] counter) {

        ByteBuffer bb = ByteBuffer.wrap(counter);
        int count = bb.getInt();
        int prevCounter = getCounter();
        boolean valid = false;
        if ((count-1)==prevCounter){
            valid = true;
            incrementCounter(count);
        }
        return valid;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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


    public byte[] messageDigest(byte[] data){

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

    //generateSessionKey
    @RequiresApi(api = Build.VERSION_CODES.M)
    byte[] generateAESKey() throws NoSuchAlgorithmException, KeyStoreException, IOException,
            CertificateException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyStore ks = KeyStore.getInstance(Constants.KEYSTORE_PROVIDER);
        ks.load(null);
        KeyGenerator keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
                Constants.KEYSTORE_PROVIDER);

        Calendar end = Calendar.getInstance();
        end.set(2019, 1, 0);
        Calendar start = Calendar.getInstance();
        start.set(2017, 11, 0);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(Constants.AES_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setCertificateSubject(new X500Principal("CN = Secured Preference Store, " +
                        "O = Devliving Online"))
                .setCertificateSerialNumber(BigInteger.ONE)
                .setKeySize(Constants.LENGHT)
                .setKeyValidityEnd(end.getTime())
                .setKeyValidityStart(start.getTime())
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build();
        keyGen.init(spec);

        SecretKey sKey = keyGen.generateKey();

        return sKey.getEncoded();
    }
}

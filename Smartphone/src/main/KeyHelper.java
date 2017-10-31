package main;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class KeyHelper {
	
	private byte[] key; 
	private byte[] initialVector; 
	
	public KeyHelper(){
		this.key=generateKey();
		this.initialVector=generateIV();
	}
	
	
	public byte[] getKey(){
		return key;
	}
	
	public byte[] getInitialVector(){
		return initialVector;
	}
	
	private byte[] generateKey(){
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[12]; 
		random.nextBytes(bytes);
		return bytes;
	}
	
	
	private byte[] generateIV(){
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[16]; 
		
		random.nextBytes(bytes);

		return bytes;
	}
	
	public String encrypt(byte[] key, byte[] initialVector, String value){
		
		try {
		
			IvParameterSpec initializationVector = new IvParameterSpec(initialVector);
	        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
	
	        //FIXME: advantages/disadvantages
	        //AES/CBC/NoPadding (128)
	        //AES/CBC/PKCS5Padding (128)
	        //AES/ECB/NoPadding (128)
	       
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, initializationVector);
	
	        byte[] encrypted = cipher.doFinal(value.getBytes());

	        return Base64.encodeBase64String(encrypted);
	        
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	public String decrypt(byte[] key, byte[] initialVector, String encrypted){
		
		try {	
			IvParameterSpec initializationVector = new IvParameterSpec(initialVector);
	        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			
	        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, initializationVector);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

        return null;
	}

}

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
	
	private String key; 
	private String initialVector; 
	
	public KeyHelper(){
		this.key=generateKey();
		this.initialVector=generateIV();
	}
	
	
	public String getKey(){
		return key;
	}
	
	public String getInitialVector(){
		return initialVector;
	}
	
	//FIXME:why 12?
	private String generateKey(){
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[12]; 
		random.nextBytes(bytes);
		return Base64.encodeBase64String(bytes);
	}
	
	//FIXME:why 12?
	private String generateIV(){
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[12]; 
		
		random.nextBytes(bytes);
		return Base64.encodeBase64String(bytes);
	}
	
	public String encrypt(String key, String initialVector, String value){
		
		try {
		
			IvParameterSpec initializationVector = new IvParameterSpec(initialVector.getBytes("UTF-8"));
	        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
	
	        //FIXME: advantages/disadvantages
	        //AES/CBC/NoPadding (128)
	        //AES/CBC/PKCS5Padding (128)
	        //AES/ECB/NoPadding (128)
	       
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, initializationVector);
	
	        byte[] encrypted = cipher.doFinal(value.getBytes());

	        return Base64.encodeBase64String(encrypted);
	        
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
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
	
	public String decrypt(String key, String initialVector, String encrypted){
		
		try {	
			IvParameterSpec initializationVector = new IvParameterSpec(initialVector.getBytes("UTF-8"));
	        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			
	        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, initializationVector);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
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

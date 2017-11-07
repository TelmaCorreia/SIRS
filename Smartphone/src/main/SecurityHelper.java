package main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.MessageDigest;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


/*
 * ordem nas msgs: hash + nounce + msg
 * msg = [FKEY| PING | SKEY | STOP | AUTH]
 * Nonce = timestamp (nº de segundos (long  litle endian)) 
 * IV vai junto com a section key (tamanho: 16 bytes *CONFIGURÁVEL*)
 * */
public class SecurityHelper {
	
	private byte[] fileKey; 
	private byte[] sessionKey;
	
	
	public SecurityHelper(){
		this.fileKey=generateKey();
		this.sessionKey = generateSessionKey();
	}
	
	
	public byte[] getKey(){
		return fileKey;
	}
	
	
	private byte[] generateKey(){
		String filename = "key.txt";
		File keyFile = new File(filename);
		try {
			if (!(keyFile.exists() && !keyFile.isDirectory())){
				keyFile.createNewFile();
				System.out.println("not exist");
				SecureRandom random = new SecureRandom();
				byte bytes[] = new byte[16]; 
				random.nextBytes(bytes);
				Path file = Paths.get(filename);
				Files.write(file,bytes);
				return bytes;
			}else{
				System.out.println("exist");
				byte[] key = new byte[(int) keyFile.length()];
				InputStream is = new FileInputStream(keyFile);
				is.read(key);
		        is.close();
		        return key;
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;

	}
	
	private byte[] generateIV(){
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[16]; 
		
		random.nextBytes(bytes);

		return bytes;
	}
	
	private byte[] generateSessionKey(){
		
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[16]; 
		random.nextBytes(bytes);
		return bytes;

	}
	
	public byte[] generateNonce(){
		long timestamp = System.currentTimeMillis() / 1000;
		ByteBuffer nounce = ByteBuffer.allocate(Long.BYTES);
	    nounce.putLong(timestamp);
	    return nounce.array();
	}
	
	
	public byte[] signData(PrivateKey priv, byte[] data){
		byte[] realSign= null;
		try {
			//FIXME: verify algorithm
			Signature sign = Signature.getInstance("SHA2withRSA");
			sign.initSign(priv);
			sign.update(data);
			
			realSign = sign.sign();
			
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		} 		
		return realSign;
		
	}
	
	public byte[] MessageDigest(byte[] data){
		byte[] dataDigest = null;
		 try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(data);
		    MessageDigest dataMD = (java.security.MessageDigest) md.clone();
		    dataDigest = dataMD.digest();
		 } catch (CloneNotSupportedException cnse) {
		     System.out.println("couldn't make digest of partial content");
		 } catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		 
		return dataDigest;
		
	}
	
	
public byte[] encrypt(byte[] value){
		
		try {
			byte[] initialVector = generateIV();
			IvParameterSpec initializationVector = new IvParameterSpec(initialVector);
	        SecretKeySpec secretKeySpec = new SecretKeySpec(this.sessionKey, "AES");
	
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, initializationVector);
	
	        byte[] encrypted = cipher.doFinal(value);

	        return encrypted;
	        
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

	public byte[] decrypt(byte[] encrypted){
		
		try {	
			byte[] initialVector = generateIV();
			byte[] key = generateSessionKey();
			IvParameterSpec initializationVector = new IvParameterSpec(initialVector);
	        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
	
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			
	        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, initializationVector);
	
	        byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
	
	        return original;
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


	public byte[] composeMsg(byte[] requestType, byte[] content) {
		byte[] nounce = generateNonce();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(nounce);
			outputStream.write(requestType);
			outputStream.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte data[] = outputStream.toByteArray( );
		byte[] hash = MessageDigest(data);
		outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(hash);
			outputStream.write(data);			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return encrypt(outputStream.toByteArray());
	}

}

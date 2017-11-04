package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
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

public class SecurityHelper {
	
	private byte[] fileKey; 
	private byte[] nonce;
	private byte[] sessionKey;
	
	public SecurityHelper(){
		this.fileKey=generateKey();
		this.nonce = generateNonce();
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
	
	private byte[] generateSessionKey(){
		
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[16]; 
		random.nextBytes(bytes);
		return bytes;

	}
	
	private byte[] generateNonce(){
		
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[16]; 
		random.nextBytes(bytes);
		return bytes;

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
	
	public void MessageDigest(byte[] data){
		
		 try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(data);
		    MessageDigest dataMD = (java.security.MessageDigest) md.clone();
		     byte[] dataDigest = dataMD.digest();
		  
		 } catch (CloneNotSupportedException cnse) {
		     System.out.println("couldn't make digest of partial content");
		 } catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}
	
	
	


}

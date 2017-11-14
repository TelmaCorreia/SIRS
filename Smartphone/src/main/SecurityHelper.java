package main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.security.MessageDigest;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/*
 * ordem nas msgs: hash + nonce + msg
 * msg = [FKEY| PING | SKEY | STOP | AUTH]
 * Nonce = timestamp (n� de segundos (long  litle endian)) 
 * IV vai junto com a section key (tamanho: LENGHT bytes *CONFIGUR�VEL*)
 * */
public class SecurityHelper {
	
	private final int LENGHT = 16;
	private final int TOLERANCE = 3;
	private final String FILENAME = "..\\public_key.der";
	private byte[] fileKey; 
	private byte[] sessionKey;
	private byte[] initializationVectorFK;
	private byte[] initializationVectorSK;
	
	
	
	public SecurityHelper(){
		this.fileKey=generateRandom("key.txt");
		this.initializationVectorFK = generateRandom("iv.txt");
		this.sessionKey = "0123456701234567".getBytes();
		this.initializationVectorSK = "0123456701234567".getBytes();
		//this.sessionKey = generateRandom();
		//this.initializationVectorSK = generateRandom();
	}
	
	
	public byte[] getFileKey(){
		return fileKey;
	}
	
	public byte[] getSessionKey(){
		return sessionKey;
	}
	
	public byte[] getIVFK(){
		return initializationVectorFK;
	}
	
	public byte[] getIVSK(){
		return initializationVectorSK;
	}
	
	private PublicKey getPublicKey() {
		try {
			
			byte[] keyBytes = Files.readAllBytes(Paths.get(FILENAME));
		    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		    KeyFactory kf = KeyFactory.getInstance("RSA");
		    return kf.generatePublic(spec);
		  
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		} catch (InvalidKeySpecException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}


	public byte[] MessageDigest(byte[] data){
		byte[] dataDigest = null;
		 try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(data);
		    MessageDigest dataMD = (java.security.MessageDigest) md.clone();
		    dataDigest = dataMD.digest();
		 } catch (CloneNotSupportedException cnse) {
		     System.out.println("couldn't make digest of partial content");
		 } catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		}
		 
		return dataDigest;
		
	}
	

	//Compose Symmetric message: (content of the message) -> hash + nonce + msg
	public byte[] composeMsgSymetricEncryption(byte[] content) {
		byte[] nonce = generateNonce();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(nonce);
			outputStream.write(content);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		byte data[] = outputStream.toByteArray( );
		

		byte[] hash = MessageDigest(data);
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
		map.put("nonce", Arrays.copyOfRange(content, 32, 40));
		map.put("data",Arrays.copyOfRange(content, 32, content.length));
		return map;
	}
	
	//Compose Asymmetric message: (content of the message) -> hash + nonce + msg
	public byte[] composeMsgAsymetricEncryption(byte[] content) {
		byte[] nonce = generateNonce();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(nonce);
			outputStream.write(content);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		byte data[] = outputStream.toByteArray( );
		byte[] hash = MessageDigest(data);
		outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(hash);
			outputStream.write(data);			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return encryptAsymmetric(outputStream.toByteArray());
	}
	
	
	//Asymmetric encryption
	public byte[] encryptAsymmetric(byte[] inputData){
		try {
	        PublicKey key= getPublicKey();
	        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS5Padding");
	        cipher.init(Cipher.PUBLIC_KEY, key);
	        byte[] encryptedBytes = cipher.doFinal(inputData);

	        return encryptedBytes;
	        
		} catch (NoSuchPaddingException e) {
			System.out.println(e.getMessage());
		} catch (InvalidKeyException e) {
			System.out.println(e.getMessage());
		} catch (IllegalBlockSizeException e) {
			System.out.println(e.getMessage());
		} catch (BadPaddingException e) {
			System.out.println(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

        return null;
 
    }
	
	//Asymmetric decryption
		public byte[] decryptAsymmetric(byte[] inputData){
	
			try {
		        PublicKey key= getPublicKey();
		        Cipher cipher = Cipher.getInstance("RSA");
		        cipher.init(Cipher.DECRYPT_MODE, key);
		        byte[] original= cipher.doFinal(inputData);

		        return original;
		        
			} catch (NoSuchPaddingException e) {
				System.out.println(e.getMessage());
			} catch (InvalidKeyException e) {
				System.out.println(e.getMessage());
			} catch (IllegalBlockSizeException e) {
				System.out.println(e.getMessage());
			} catch (BadPaddingException e) {
				System.out.println(e.getMessage());
			} catch (NoSuchAlgorithmException e) {
				System.out.println(e.getMessage());
			}

	        return null;
	 
	    }
	

	//Symmetric Encryption
	public byte[] encrypt(byte[] value){
		
		try {
			IvParameterSpec initializationVector = new IvParameterSpec(this.initializationVectorSK);
	        SecretKeySpec secretKeySpec = new SecretKeySpec(this.sessionKey, "AES");
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, initializationVector);
	        byte[] encrypted = cipher.doFinal(value);

	        return encrypted;
	        
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		} catch (NoSuchPaddingException e) {
			System.out.println(e.getMessage());
		} catch (IllegalBlockSizeException e) {
			System.out.println(e.getMessage());
		} catch (BadPaddingException e) {
			System.out.println(e.getMessage());
		} catch (InvalidKeyException e) {
			System.out.println(e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			System.out.println(e.getMessage());
		}
		return null;
		
	}
	
	
	//Symmetric Decrypt
	public byte[] decrypt(byte[] encrypted){
		
		try {	
			IvParameterSpec initializationVector = new IvParameterSpec(this.initializationVectorSK);
	        SecretKeySpec secretKeySpec = new SecretKeySpec(this.sessionKey, "AES");
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, initializationVector);
	        byte[] original = cipher.doFinal(encrypted);
	
	        return original;
	        
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			System.out.println(e.getMessage());
		} catch (IllegalBlockSizeException e) {
			System.out.println(e.getMessage());
		} catch (BadPaddingException e) {
			System.out.println(e.getMessage());
		} catch (InvalidKeyException e) {
			System.out.println(e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			System.out.println(e.getMessage());
		}
	
	    return null;
	}
	
	/*
	 * Auxiliary functions
	 * */
	
	//validate if message is fresh
	public boolean checkValidNonce(byte[] nonce){
		ByteBuffer bb = ByteBuffer.wrap(nonce);
		long timestamp = bb.getLong();
		long now = System.currentTimeMillis() / 1000;
		return Math.abs(now-timestamp)<= TOLERANCE;
	}
	
	//validate message integrity
	public boolean checkHash(byte[] hash, byte[] myHash){
		return Arrays.equals(hash, myHash);
	}
	
	//used for filekey and fileIV
	private byte[] generateRandom(String filename){
		File file = new File(filename);
		try {
			if (!(file.exists() && !file.isDirectory())){
				file.createNewFile();
				SecureRandom random = new SecureRandom();
				byte bytes[] = new byte[LENGHT]; 
				random.nextBytes(bytes);
				Path filePath = Paths.get(filename);
				Files.write(filePath,bytes);
				return bytes;
			}else{
				byte[] key = new byte[(int) file.length()];
				InputStream is = new FileInputStream(file);
				is.read(key);
		        is.close();
		        return key;
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	//used for sessionkey and sessionIV
	private byte[] generateRandom(){
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[LENGHT]; 
		random.nextBytes(bytes);
		return bytes;
	}
	
	//get timestamp and transform to byte[]
	public byte[] generateNonce(){
		long timestamp = System.currentTimeMillis() / 1000;
		ByteBuffer nonce = ByteBuffer.allocate(Long.BYTES);
	    nonce.putLong(timestamp);
	    return nonce.array();
	}
	


  
	
		 
}

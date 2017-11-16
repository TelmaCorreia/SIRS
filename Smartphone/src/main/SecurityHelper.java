package main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
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
 * Nonce = timestamp (nº de segundos (long  litle endian)) 
 * IV vai junto com a section key (tamanho: LENGHT bytes *CONFIGURÁVEL*)
 * */
public class SecurityHelper {
	
	private final int LENGHT = 16;
	private final int TOLERANCE = 3;
	private final String FILENAME = "..\\public_key.der";
	private byte[] fileKey; 
	private byte[] sessionKey;
	private byte[] initializationVectorFK;
	private byte[] initializationVectorSK;
	private int counter;
	
	
	
	public SecurityHelper(){
		this.fileKey=generateRandom("key.txt");
		this.initializationVectorFK = generateRandom("iv.txt");
		this.sessionKey = generateRandom();
		this.initializationVectorSK = generateRandom();
		this.counter=0;
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
	
	public int getCounter(){
		return counter;
	}
	

	public void incrementCounter(int count){
		this.counter= count+1;
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
		map.put("nonce", Arrays.copyOfRange(content, 32, 32+4));
		map.put("counter", Arrays.copyOfRange(content, 36, 36+4));
		map.put("data",Arrays.copyOfRange(content, 32, content.length));
		//System.out.println(map.get("hash"));
		//System.out.println(map.get("nonce"));
		//System.out.println(map.get("data"));

		return map;
	}
	
	public HashMap<String, byte[]> decomposeMsgAsymmetricEncryption(byte[] content) {
		HashMap<String, byte[]> map = new HashMap();
		map.put("hash", Arrays.copyOfRange(content, 0, 256));
		map.put("nonce", Arrays.copyOfRange(content, 256, 256+4));
		map.put("counter", Arrays.copyOfRange(content, 260, 260+4));
		map.put("data",Arrays.copyOfRange(content, 256, content.length));
		//System.out.println(map.get("hash"));
		//System.out.println(map.get("nonce"));
		//System.out.println(map.get("data"));

		return map;
	}
	
	//Compose Asymmetric message: (content of the message) -> hash + nonce + msg
	public byte[] composeMsgAsymetricEncryption(byte[] content) {
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
	        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
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
		        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
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
		int timestamp = bb.getInt();
		int now = (int) (System.currentTimeMillis() / 1000);
		boolean valid =  Math.abs(now-timestamp)<= TOLERANCE;
		System.out.println("Valid = " + valid + " ts = "+timestamp + " now = " +now);
		return valid;
	}
	
	//validate message integrity
	public boolean checkHash(byte[] hash, byte[] myHash){
		boolean valid = Arrays.equals(hash, myHash);
		System.out.println("Valid = " + valid);
		return valid;

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
		int timestamp = (int) (System.currentTimeMillis() / 1000);
		System.out.println("nonce: " +timestamp);
		ByteBuffer nonce = ByteBuffer.allocate(Integer.BYTES);
	    nonce.putInt(timestamp);
	    return nonce.array();
	}


	public boolean verifySignature(byte[] signature, byte[] data) {
		Signature signature1;
		try {
			signature1 = Signature.getInstance("SHA256withRSA");
			signature1.initVerify(getPublicKey());
			signature1.update(data);
			boolean result = signature1.verify(signature);
			return result;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}


	public boolean checkValidCounter(byte[] counter) {
	    
	   ByteBuffer bb = ByteBuffer.wrap(counter);
	   // bb.order(ByteOrder.LITTLE_ENDIAN);
	   int count = bb.getInt();
	   int prevCounter = getCounter();
	   boolean valid = false;
	   if ((count-1)==prevCounter){
		   valid = true;
		   incrementCounter(count);
	   }
		
		return valid;
	}
	


  
	
		 
}

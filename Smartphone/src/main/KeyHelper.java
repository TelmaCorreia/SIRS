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
	}
	
	
	public byte[] getKey(){
		return key;
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
	
	


}

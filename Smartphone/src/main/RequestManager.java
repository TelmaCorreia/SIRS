package main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

public class RequestManager {
	
	public static final String START_CONNECTION ="";
	public static final String PING = "PING";
	public static final String FKEY = "FKEY";
	public static final String STOP = "STOP";
	private SecurityHelper sh;
	
	public RequestManager(){
		 
		this.sh=new SecurityHelper();
	}
	
	public SecurityHelper getSecurityHelper(){
		return sh;
	}
	
	
	public String startConnection(String requestType){
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(getSecurityHelper().getSessionKey());
			outputStream.write(getSecurityHelper().getIVSK());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] content = getSecurityHelper().composeMsgAsymetricEncryption(outputStream.toByteArray());
		return Base64.encodeBase64String(content);
	}
	public String sendFKEY(String requestType){
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(requestType.getBytes());
			outputStream.write(getSecurityHelper().getFileKey());
		//	outputStream.write(getSecurityHelper().getIVFK());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] content = getSecurityHelper().composeMsgSymetricEncryption(outputStream.toByteArray());
		
		return  Base64.encodeBase64String(content);
		
		
	}

	public String sendSimpleMessage(String requestType) {
		byte[] content = getSecurityHelper().composeMsgSymetricEncryption(requestType.getBytes());
		return  Base64.encodeBase64String(content);
	}

	public String generateMessage(String requestType) {
		
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

	public boolean processResponse(String requestType, String response) {
		
		//decrypt message
		byte[] decryptedResponse = getSecurityHelper().decrypt(Base64.decodeBase64(response));
		//decompose message
		if (requestType.equals(START_CONNECTION)){
			HashMap<String, byte[]> map = getSecurityHelper().decomposeMsgAsymmetricEncryption(decryptedResponse);
			//byte[] hash = map.get("hash");
			//byte[] decryptedHash = getSecurityHelper().decryptAsymmetric(hash);
			//map.put("hash", decryptedHash);
			return validateResponseStart(map);
		}else{
			HashMap<String, byte[]> map = getSecurityHelper().decomposeMsgSymmetricEncryption(decryptedResponse);

			return validateResponse(map);
			
		}
				
	}

	private boolean validateResponse(HashMap<String, byte[]> map) {
		
		boolean nonce = getSecurityHelper().checkValidNonce(map.get("nonce"));
		boolean counter = getSecurityHelper().checkValidCounter(map.get("counter"));
		byte [] myhash = getSecurityHelper().MessageDigest(map.get("data"));
		boolean hash = getSecurityHelper().checkHash(map.get("hash"), myhash);
	
		return (nonce && hash && counter);
		
	}
	
private boolean validateResponseStart(HashMap<String, byte[]> map) {
		
		boolean nonce = getSecurityHelper().checkValidNonce(map.get("nonce"));
		boolean counter = getSecurityHelper().checkValidCounter(map.get("counter"));
		boolean hash  = getSecurityHelper().verifySignature(map.get("hash"), map.get("data"));
	
		return (nonce && hash && counter);
		
	}

	
	
	

}

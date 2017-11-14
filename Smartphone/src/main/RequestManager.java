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
		byte[] content = getSecurityHelper().composeMsgAsymetricEncryption(getSecurityHelper().getSessionKey());
		return Base64.encodeBase64String(content);
	}
	public String sendFKEY(String requestType){
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(requestType.getBytes());
			outputStream.write(getSecurityHelper().getFileKey());
			outputStream.write(getSecurityHelper().getIVFK());
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
		HashMap<String, byte[]> map = getSecurityHelper().decomposeMsgSymmetricEncryption(decryptedResponse);
		if (requestType.equals(START_CONNECTION)){
			byte[] hash = map.get("hash");
			//byte[] decryptedHash = getSecurityHelper().decryptAsymmetric(hash);
			//map.put("hash", decryptedHash);
			return validateResponse(map);
		}else{
			return validateResponse(map);
			
		}
				
	}

	private boolean validateResponse(HashMap<String, byte[]> map) {
		
		boolean nonce = getSecurityHelper().checkValidNonce(map.get("nonce"));
		byte [] myhash = getSecurityHelper().MessageDigest(map.get("data"));
		boolean hash = getSecurityHelper().checkHash(map.get("hash"), myhash);
	
		return (nonce && hash);
		
	}

	public String test(String string) {
		// TODO Auto-generated method stub
		return null;
	}
	 
	
	

}

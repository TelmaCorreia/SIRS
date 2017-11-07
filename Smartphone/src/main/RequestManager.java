package main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class RequestManager {
	
	public String sendFKEY(String requestType){
		SecurityHelper kh = new SecurityHelper();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(requestType.getBytes());
			outputStream.write(kh.getKey());
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] content = kh.composeMsg(outputStream.toByteArray());
		return Base64.getEncoder().encodeToString(content);
		
	}

	public String sendPING(String requestType) {
		SecurityHelper kh = new SecurityHelper();
		byte[] content = kh.composeMsg(requestType.getBytes());
		return Base64.getEncoder().encodeToString(content);
	}

	public String sendSTOP(String requestType) {
		SecurityHelper kh = new SecurityHelper();
		byte[] content = kh.composeMsg(requestType.getBytes());
		return Base64.getEncoder().encodeToString(content);
	}

}

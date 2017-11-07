package main;

import java.util.Base64;

public class RequestManager {
	
	public String sendFKEY(String requestType){
		SecurityHelper kh = new SecurityHelper();
		byte[] content = kh.composeMsg(requestType.getBytes(),  kh.getKey());
		return Base64.getEncoder().encodeToString(content);
		
	}

	public String sendPING(String requestType) {
		// TODO Auto-generated method stub
		return "";
	}

	public String sendSTOP(String requestType) {
		// TODO Auto-generated method stub
		return "";
	}

}

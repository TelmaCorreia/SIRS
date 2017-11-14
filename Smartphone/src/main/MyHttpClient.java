package main;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.commons.codec.binary.Base64;


public class MyHttpClient {
	public static final String START_CONNECTION ="";
	public static final String PING = "PING";
	public static final String FKEY = "FKEY";
	public static final String STOP = "STOP";
	public static final int TIME_INTERVAL = 3000;

  	private RequestManager rm; 

	public MyHttpClient(){
		  rm = new RequestManager();
	}
	
	public RequestManager getRequestManager(){
		return rm;
	}
  	
	private HttpClient client = HttpClientBuilder.create().build();

	public static void main(String[] args) throws Exception {
	  
		String url = "http://localhost";

		System.out.println("*** client started ***");

		MyHttpClient http = new MyHttpClient();

		//http.testSymmetric();
		
		try{
			http.post(url, START_CONNECTION);
			http.post(url, FKEY);
			while(true){
				http.post(url, PING);
				Thread.sleep(TIME_INTERVAL);
			}
			//http.post(url, STOP);
		}catch( org.apache.http.conn.HttpHostConnectException e){
			System.out.println(e.getMessage());
		}
		
		
		System.out.println("*** client terminated ***");
  }
	

	  
	  private void testSymmetric() {
		  		  
		  String msg = getRequestManager().sendFKEY(FKEY);
		  boolean valid = getRequestManager().processResponse(FKEY, msg);
		  System.out.println("Valid: "+ valid );
		  
	}
	

	private void post(String url, String requestType) throws Exception {
		  	
			try{
				String msg = "";
				HttpPost post = new HttpPost(url);
				
				msg = rm.generateMessage(requestType);
				post.setHeader("content-lenght",String.valueOf(msg.length()));
				post.setEntity(new StringEntity(msg));
			
				System.out.println("\nSending 'POST' request to URL : " + url);
				System.out.println("RequestType : " + requestType);
				System.out.println("Post parameters : " + msg);
				System.out.println("content-lenght : " + msg.length());

				
				HttpResponse response = client.execute(post);
		
				int responseCode = response.getStatusLine().getStatusCode();
				
				System.out.println("Response Code : " + responseCode);
			
				BufferedReader rd = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));
			
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				
				//TODO
				System.out.println("Request Type: "+requestType);
				System.out.println("Result: "+(result.toString()));
	
				if(!rm.processResponse(requestType, new String(result))){
					System.out.println("*** WARNING: Ivalid Response! ***");
					System.exit(0) ; 
				};
				
			}catch(ConnectTimeoutException e){
				//If timeout, stop connection
				System.out.println(e.getMessage());
				return;
		    	
		    }
	
	  }


}
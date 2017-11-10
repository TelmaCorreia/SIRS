package main;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;


public class MyHttpClient {
	public static final String START_CONNECTION ="CONNECT";
	public static final String PING = "PING";
	public static final String FKEY = "FKEY";
	public static final String STOP = "STOP";
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

		http.testSymetric();
		
		try{
			//http.post(url, START_CONNECTION);
			http.post(url, FKEY);
			http.post(url, PING);
			http.post(url, STOP);
		}catch( org.apache.http.conn.HttpHostConnectException e){
			System.out.println(e.getMessage());
		}
		
		
		System.out.println("*** client terminated ***");
  }
	

	  
	  private void testSymetric() {
		  		  
		  String msg = getRequestManager().sendFKEY(FKEY);
		  boolean valid = getRequestManager().processResponse(FKEY, msg);
		  System.out.println("Valid: "+ valid );
		  
	}
	  

	private void post(String url, String requestType) throws Exception {
		  	String msg = "";
			HttpPost post = new HttpPost(url);
			
			msg = rm.generateMessage(requestType);
			post.setHeader("content-lenght",String.valueOf(msg.length()));
			post.setEntity(new StringEntity(msg));
		
			HttpResponse response = client.execute(post);
	
			int responseCode = response.getStatusLine().getStatusCode();
			
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + msg);
			System.out.println("Response Code : " + responseCode);
		
			BufferedReader rd = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));
		
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			
			//TODO
			System.out.println("Request Type: "+requestType);
			System.out.println("Result: "+new String(result.toString()));

			boolean valid = rm.processResponse(requestType, new String(result));
		
	
	  }


}
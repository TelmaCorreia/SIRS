package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;


public class MyHttpClient {
	private static final String PING = "PING";
	private static final String FKEY = "FKEY";
	private static final String STOP = "STOP";

	
	private HttpClient client = HttpClientBuilder.create().build();

	  public static void main(String[] args) throws Exception {
	
			String url = "http://localhost";
		
			MyHttpClient http = new MyHttpClient();
		
			http.sendPost(url, FKEY);
			http.sendPost(url, PING);
			http.sendPost(url, STOP);
		
		
			System.out.println("Done");
	  }
	

	  
	  private void sendPost(String url, String requestType) throws Exception {
		  	String msg = requestType;
			HttpPost post = new HttpPost(url);
			
			if(requestType.equals(FKEY)){
				SecurityHelper kh = new SecurityHelper();
				msg = msg + Base64.getEncoder().encodeToString(kh.getKey());
			}
			
			
			post.setHeader("content-lenght",String.valueOf(msg.length()));
			post.setEntity(new StringEntity(msg));
		
			HttpResponse response = client.execute(post);
	
			int responseCode = response.getStatusLine().getStatusCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + msg);
			System.out.println("Response Code : " + responseCode);
		
			BufferedReader rd = new BufferedReader(
		                new InputStreamReader(response.getEntity().getContent()));
		
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
		
			 System.out.println("Result: "+result.toString());
	
	  }
	  

}
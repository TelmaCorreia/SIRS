package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class MyHttpClient {
	
	private static final String USER_AGENT = "SMARTPHONE";

	public void GETrequest(){
		
		String url = "localhost";
		try {
			
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			String msg = "";
			// add request header
			request.addHeader("User-Agent", USER_AGENT);
			request.addHeader("content-lenght",String.valueOf(msg.length()));
			
			HttpResponse response = client.execute(request);

			System.out.println("Response Code : "
			                + response.getStatusLine().getStatusCode());
			
			BufferedReader  rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));
			
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			
		} catch (UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public void POSTrequest(){
		 HttpClient client = new DefaultHttpClient();
		 
	     HttpPost post = new HttpPost("localhost");
        try {
        	String msg = "Ola";
			post.addHeader("User-Agent", USER_AGENT);
            post.addHeader("content-lenght",String.valueOf(msg.length()));
            post.setEntity(new StringEntity(msg));

            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

		
	
	
}

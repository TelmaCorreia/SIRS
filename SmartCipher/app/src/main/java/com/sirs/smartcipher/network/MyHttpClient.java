package com.sirs.smartcipher.network;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.sirs.smartcipher.RequestManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by telma on 11/21/2017.
 */

public class MyHttpClient extends AsyncTask<String, String, String[]> {

    private static final String TAG = "running";

    private RequestManager rm;

    public MyHttpClient() throws CertificateException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException,
            UnrecoverableEntryException, IOException {
        rm = new RequestManager();
    }

    @Override
    protected String[] doInBackground(String... strings) {
        String msg = "";
        HttpPost post = new HttpPost(strings[0]);

        String requestType = strings[1];
        try {
            msg = rm.generateMessage(requestType);
            post.setHeader("content-lenght",String.valueOf(msg.length()));
            post.setEntity(new StringEntity(msg));

            Log.d(TAG, "\nSending 'POST' request to URL : " + strings[0]);
            System.out.println("RequestType : " +  requestType);
            System.out.println("Post parameters : " + msg);
            System.out.println("content-lenght : " + msg.length());


            HttpResponse response = client.execute(post);

            int responseCode = response.getStatusLine().getStatusCode();

            Log.d(TAG, "Response Code : " + responseCode);

            BufferedReader rd = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            //TODO
            Log.d(TAG, "Request Type: "+requestType);
            Log.d(TAG, "Result: "+(result.toString()));
            String[] s = new String[2];
            s[0] = requestType;
            s[1] = new String(result);
            return s;
        } catch (InvalidKeySpecException e1) {
            e1.printStackTrace();
        } catch (NoSuchProviderException e1) {
            e1.printStackTrace();
        } catch (CertificateException e1) {
            e1.printStackTrace();
        } catch (NoSuchPaddingException e1) {
            e1.printStackTrace();
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (InvalidAlgorithmParameterException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (KeyStoreException e1) {
            e1.printStackTrace();
        } catch (UnrecoverableEntryException e1) {
            e1.printStackTrace();
        } catch (IllegalBlockSizeException e1) {
            e1.printStackTrace();
        } catch (BadPaddingException e1) {
            e1.printStackTrace();
        }
        return new String[1];
    }

    protected void onPostExecute(String[] result) {
        try {
            if(!rm.processResponse(result[0], result[1])){
                Log.d(TAG, "*** WARNING: Ivalid Response! ***");
                System.exit(0) ;
            }
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ;    }



    public RequestManager getRequestManager(){
        return rm;
    }

    private HttpClient client = HttpClientBuilder.create().build();

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void post(String url, String requestType) throws Exception {



        try{
            String msg = "";
            HttpPost post = new HttpPost(url);

            msg = rm.generateMessage(requestType);
            post.setHeader("content-lenght",String.valueOf(msg.length()));
            post.setEntity(new StringEntity(msg));

            Log.d(TAG, "\nSending 'POST' request to URL : " + url);
            System.out.println("RequestType : " + requestType);
            System.out.println("Post parameters : " + msg);
            System.out.println("content-lenght : " + msg.length());


            HttpResponse response = client.execute(post);

            int responseCode = response.getStatusLine().getStatusCode();

            Log.d(TAG, "Response Code : " + responseCode);

            BufferedReader rd = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            //TODO
            Log.d(TAG, "Request Type: "+requestType);
            Log.d(TAG, "Result: "+(result.toString()));

            if(!rm.processResponse(requestType, new String(result))){
                Log.d(TAG, "*** WARNING: Ivalid Response! ***");
                System.exit(0) ;
            };

        }catch(ConnectTimeoutException e){
            //If timeout, stop connection
            e.printStackTrace();
            return;

        }

    }

}

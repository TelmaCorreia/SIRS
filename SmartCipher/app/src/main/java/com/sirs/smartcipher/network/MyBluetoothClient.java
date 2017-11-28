package com.sirs.smartcipher.network;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;


import com.sirs.smartcipher.Constants;
import com.sirs.smartcipher.RequestManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Set;
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




public class MyBluetoothClient extends Thread { //extends AsyncTask<String, String, String[]> {

    class Connection {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public Connection(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;

        }

        public void connect() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            // The connection attempt succeeded.
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }

        //https://developer.android.com/guide/topics/connectivity/bluetooth.html#ManagingAConnection
        public void send(String message) {
            ; // TODO
        }

        // https://developer.android.com/guide/topics/connectivity/bluetooth.html#ManagingAConnection
        public String receive() {
            ; // TODO
            return null;
        }

    }

    private static final String TAG = "running";

    java.util.UUID MY_UUID = null; // FIXME
    public static final int TIME_INTERVAL = 3000;

    private RequestManager rm;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice pc;
    Connection connection;

    private Boolean active;

    public MyBluetoothClient(Boolean active) throws CertificateException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException,
            UnrecoverableEntryException, IOException {

        rm = new RequestManager();
        this.active = active;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            // TODO log this
        }

        if (!mBluetoothAdapter.isEnabled()) {
            // TODO log this
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        pc = getDevice();
    }

    // FIXME select the right (?) bluetooth device. First? Selected by user?
    private BluetoothDevice getDevice() {

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }

        return null; // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    @Override
    public void run() {
        connection = new Connection(pc);
        connection.connect();
        String url= "http://10.0.2.2";
        try{
            String[] response;
            response = this.post_request(url, Constants.START_CONNECTION);
            processResponse(response);
            response = post_request(url, Constants.FKEY);
            processResponse(response);
            while(active){
                response = post_request(url, Constants.PING);
                processResponse(response);
                Thread.sleep(TIME_INTERVAL);
            }
            //http.post(url, STOP);
        }
        catch (Exception e) {
            //TODO FIXME I'm very sorry for all Exceptions
            System.out.println(e.getMessage());
        }
    }

//    @Override
    protected String[] post_request(String... strings) {
        String msg = "";
        HttpPost post = new HttpPost(strings[0]);

        String requestType = strings[1];
        try {
            //HttpResponse response = client.execute(post);

            msg = rm.generateMessage(requestType);
            connection.send(msg);
            String result = connection.receive();
            String[] s = new String[2];
            s[0] = requestType;
            s[1] = result;
            return s;

/*
            post.setHeader("content-lenght",String.valueOf(msg.length()));
            post.setEntity(new StringEntity(msg));

            Log.d(TAG, "\nSending 'POST' request to URL : " + strings[0]);
            System.out.println("RequestType : " +  requestType);
            System.out.println("Post parameters : " + msg);
            System.out.println("content-lenght : " + msg.length());




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
            */
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

    protected void processResponse(String[] result) {
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

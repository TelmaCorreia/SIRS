package com.sirs.smartcipher.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import com.sirs.smartcipher.Constants;
import com.sirs.smartcipher.MyApp;
import com.sirs.smartcipher.RequestManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by telma on 11/21/2017.
 */


public class MyBluetoothClient extends Thread {

    private static final String TAG = "running";

    java.util.UUID MY_UUID = UUID.fromString("1e0ca4ea-299d-4335-93eb-27fcfe7fa848");
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
            Toast toast = Toast.makeText(MyApp.getAppContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT);
            toast.show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Toast toast = Toast.makeText(MyApp.getAppContext(), "Bluetooth will be enabled", Toast.LENGTH_SHORT);
            toast.show();
            mBluetoothAdapter.enable();
        }

        pc = getDevice();
    }

    // FIXME select the right (?) bluetooth device. First? Selected by user?
    private BluetoothDevice getDevice() {

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Map<String, BluetoothDevice> map = new HashMap<>();
        Log.d(TAG, "Paired devices: " + pairedDevices.size());
        String name = "";
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, "Device Name: " + deviceName);
                Log.d(TAG, "Device MAC: " + deviceHardwareAddress);
                map.put(deviceName, device);
                //FIXME? return first
                return device;
            }
        }

        return null;
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

    protected String[] post_request(String... strings) {
        String msg = "";
        HttpPost post = new HttpPost(strings[0]);

        String requestType = strings[1];
        try {

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
    }

}
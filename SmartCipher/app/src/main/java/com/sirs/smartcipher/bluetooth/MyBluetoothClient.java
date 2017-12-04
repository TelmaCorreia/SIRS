package com.sirs.smartcipher.bluetooth;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.sirs.smartcipher.Constants;
import com.sirs.smartcipher.MyApp;
import com.sirs.smartcipher.Core;
import com.sirs.smartcipher.ui.MainActivity;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.HttpPost;

import java.net.ConnectException;
import java.util.ArrayList;
import java.io.IOException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MyBluetoothClient extends Thread{

    private static final String TAG = "running";

    java.util.UUID MY_UUID = UUID.fromString("1e0ca4ea-299d-4335-93eb-27fcfe7fa848");
    public static final int TIME_INTERVAL = 3000;

    private Core rm;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice pc;
    Connection connection;

    private Boolean active;
    private Handler handler;

    public MyBluetoothClient(Boolean active, Handler handler) throws CertificateException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException,
            UnrecoverableEntryException, IOException {
        rm = new Core();
        this.active = active;
        this.handler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        pc = getDevice();
    }

    // FIXME select the right (?) bluetooth device. First? Selected by user?
    private BluetoothDevice getDevice() {

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        final Map<String, BluetoothDevice> map = new HashMap<>();
        Log.d(TAG, "Paired devices: " + pairedDevices.size());
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, "Device Name: " + deviceName);
                Log.d(TAG, "Device MAC: " + deviceHardwareAddress);
                map.put(deviceHardwareAddress, device);
            }

            Context context = MyApp.getAppContext();
            SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREF_MAC, Context.MODE_PRIVATE);
            String mac = sharedPref.getString(Constants.SHARED_PREF_KEY_MAC, "");
            if (mac!="") return map.get(mac);
        }

        return null;



    }


    @Override
    public void run() {
        Looper.prepare();
        try{

            try {
                connection = new Connection(pc);
                connection.connect();

            } catch (Exception e) {
                e.printStackTrace();
                throw new MyException("Connection failed. Please configure your device!");
            }

            String[] response;
            try {
                response = this.post_request(Constants.START_CONNECTION);
                processResponse(response);
            }catch (Exception e ){
                throw new MyException("Connection failed. Please configure your public key!");
            }
            response = post_request(Constants.FKEY);
            processResponse(response);
            while(active){
                response = post_request(Constants.PING);
                processResponse(response);
                Thread.sleep(TIME_INTERVAL);
            }
            post_request(Constants.STOP);
        }
        catch (MyException e){
            e.printStackTrace();
            final String msg = e.getMessage();

            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(MyApp.getAppContext(),"Error: " +msg, Toast.LENGTH_LONG).show();
                }
            });

            handler.sendEmptyMessage(0);
        }
        catch (Exception e) {
            e.printStackTrace();
            final String msg = e.getMessage();
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(MyApp.getAppContext(),"Error: " +msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    protected String[] post_request(String... strings) throws MyException {
        String msg = "";

        String requestType = strings[0];
        try {

            msg = rm.generateMessage(requestType);
            connection.send(msg);
            String result = connection.receive(Constants.MESSAGE_SIZE);
            String[] s = new String[2];
            s[0] = requestType;
            s[1] = result;
            return s;

        } catch (Exception e) {
            e.printStackTrace();
            throw new MyException(e.getMessage());
        }
    }

    protected void processResponse(String[] result) throws MyException {
        try {
            if(!rm.processResponse(result[0], result[1])){
                Log.d(TAG, "*** WARNING: Ivalid Response! ***");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MyException(e.getMessage());
        }
    }

    public void setActiveFalse() {
        active=false;
    }
}

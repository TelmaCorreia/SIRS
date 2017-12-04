package com.sirs.smartcipher.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.sirs.smartcipher.MyApp;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by telma on 12/1/2017.
 */

public class Connection {

    private static final String TAG = "CONNECTION" ;
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter mBluetoothAdapter;
    java.util.UUID MY_UUID = UUID.fromString("1e0ca4ea-299d-4335-93eb-27fcfe7fa848");


    public Connection(BluetoothDevice device) {
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);

        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void connect() {
       // mBluetoothAdapter.cancelDiscovery();
        try {

            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
    }

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

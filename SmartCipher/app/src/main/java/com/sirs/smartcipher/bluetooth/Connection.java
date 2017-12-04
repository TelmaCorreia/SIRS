package com.sirs.smartcipher.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.sirs.smartcipher.MyApp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;



/**
 * Created by telma on 12/1/2017.
 */

public class Connection {

    private static final String TAG = "CONNECTION" ;
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private BluetoothAdapter mBluetoothAdapter;
    java.util.UUID MY_UUID = UUID.fromString("1e0ca4ea-299d-4335-93eb-27fcfe7fa848");


    public Connection(BluetoothDevice device) throws IOException {
        BluetoothSocket tmp = null;
        mmDevice = device;


        try {
            if (device!=null){
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            }else{
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(MyApp.getAppContext(),"No device connected, please try again", Toast.LENGTH_LONG).show();
                    }
                });
            }

        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
            throw e;
        }
        mmSocket = tmp;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = mmSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
            throw e;
        }
        try {
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
            throw e;
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void connect() throws IOException {
       // mBluetoothAdapter.cancelDiscovery();
        try {

            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
                throw e;
            }
            throw connectException;

        }
    }

    public void cancel() throws IOException {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
            throw e;

        }
    }

    public void send(String message) throws IOException {
        byte[] msg =  Base64.decode(message, Base64.DEFAULT);
        send(msg);
    }

    public void send(byte[] bytes) throws IOException {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
            throw e;

        }
    }

    // https://developer.android.com/guide/topics/connectivity/bluetooth.html#ManagingAConnection
    public String receive(int nToRead) throws IOException {
        byte[] mmBuffer = new byte[nToRead];
        ByteBuffer buffer = ByteBuffer.allocate(nToRead);
        int numBytes; // bytes returned from read()
        int already_read = 0;

        //buffer.put()

        // Keep listening to the InputStream until an exception occurs.
        while (already_read < nToRead) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                buffer.put(mmBuffer, already_read, numBytes);
                already_read += numBytes;

            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                throw e;
            }
        }
        return Base64.encodeToString(buffer.array(), Base64.DEFAULT);
    }
}

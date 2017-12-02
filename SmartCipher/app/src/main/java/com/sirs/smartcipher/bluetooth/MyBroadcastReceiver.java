package com.sirs.smartcipher.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Collection;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "Broadcast Receiver";
    Collection<String> mArrayAdapter;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Add the name and address to an array adapter to show in a ListView
            mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        }
        if(intent.getAction().equals("android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED")){
            Log.d(TAG,"Bluetooth connect");
        }
    }
}

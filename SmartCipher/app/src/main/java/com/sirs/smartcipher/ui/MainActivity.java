package com.sirs.smartcipher.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sirs.smartcipher.Constants;
import com.sirs.smartcipher.MyApp;
import com.sirs.smartcipher.R;
import com.sirs.smartcipher.bluetooth.MyBluetoothClient;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "running";
    Button btn_start, btn_key, btn_stop;
    TextView tv_log;
    int count = 0;
    Boolean active = false;
    private Handler mHandler;
    private MyBluetoothClient b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message m){
                stop();
            }
        };

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        tv_log = (TextView) findViewById(R.id.tv_log);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("teste", "start");

                btn_start.setVisibility(View.GONE);
                btn_stop.setVisibility(View.VISIBLE);
                Date date = new Date();
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                tv_log.setText(df.format(date) + ": Connection Started");
                try{
                    active=true;

                    b = new MyBluetoothClient(active, handler);
                    b.start();

                } catch (Exception e) {
                    Toast.makeText(MyApp.getAppContext(),"Connection failed. Please configure your device!", Toast.LENGTH_LONG).show();
                    stop();
                }


            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.layout_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_publicKey:
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_device:
                showDialog(getDevices());
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showDialog(final Map<String, BluetoothDevice> map) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setTitle("Select One Device:");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
        for (String device : map.keySet()) {
            arrayAdapter.add(device);
        }

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                String mac = map.get(strName).getAddress();
                saveMACAddress(mac);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }


    private Map<String, BluetoothDevice> getDevices() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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
            }
        }
        return map;
    }

    private void saveMACAddress(String macAddress){
        Context context = MyApp.getAppContext();
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREF_MAC, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.SHARED_PREF_KEY_MAC, macAddress);
        editor.commit();


    }


public void stop(){

    btn_start.setVisibility(View.VISIBLE);
    btn_stop.setVisibility(View.GONE);
    active=false;
    b.setActiveFalse();
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    tv_log.setText(tv_log.getText()+"\n" +df.format(date) + ": Connection Stoped");

}


}





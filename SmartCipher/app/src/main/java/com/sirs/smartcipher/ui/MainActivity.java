package com.sirs.smartcipher.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        btn_key = (Button) findViewById(R.id.btn_key);
        tv_log = (TextView) findViewById(R.id.tv_log);


        btn_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(intent);
            }
        });

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





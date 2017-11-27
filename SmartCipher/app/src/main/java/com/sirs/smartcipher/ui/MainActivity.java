package com.sirs.smartcipher.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sirs.smartcipher.Constants;
import com.sirs.smartcipher.R;
import com.sirs.smartcipher.network.MyHttpClient;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "running";
    Button btn;
    int count =0;
    Boolean active = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.btn_start);
        btn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.O)
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                String url = "http://10.0.2.2";

                Log.d(TAG, "*** client started ***");

                try {

                    if(!active) {
                        active = true;
                        MyHttpClient http = new MyHttpClient(active);
                        http.start();

                    }
                    else {
                        ; // Do nothing
                    }
                    return;
/*
                    MyHttpClient http = new MyHttpClient();

                    if(count==0){ count ++; http.execute(url, Constants.START_CONNECTION);}
                    else if(count==1){  http.execute(url, Constants.FKEY);}
                    else{http.execute(url, Constants.PING);}
//
//                    http.execute(url, Constants.START_CONNECTION);
//                    http.execute(url, Constants.FKEY);
//                    while (true) {
//                       http.execute(url, Constants.PING);
//                        Thread.sleep(Constants.TIME_INTERVAL);
//                    }
*/
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "*** client terminated ***");
            }
        });

    }
}





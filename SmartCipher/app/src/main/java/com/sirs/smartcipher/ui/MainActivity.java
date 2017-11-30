package com.sirs.smartcipher.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sirs.smartcipher.Constants;
import com.sirs.smartcipher.QRActivity;
import com.sirs.smartcipher.R;
import com.sirs.smartcipher.network.MyHttpClient;

import org.w3c.dom.Text;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "running";
    Button btn_start, btn_key;
    TextView tv_log;
    int count =0;
    Boolean active = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_key = (Button) findViewById(R.id.btn_key);
        tv_log = (TextView)findViewById(R.id.tv_log);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.O)
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                String url = "http://10.0.2.2";
                Log.d(TAG, "*** client started ***");
                String text = (String) tv_log.getText();
                tv_log.setText(text + "Connection Started");
                try {

                    if(!active) {
                        active = true;
                        MyHttpClient http = new MyHttpClient(active);
                        http.start();

                    }
                    else {
                        // Do nothing
                    }
                    return;

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "*** client terminated ***");
            }
        });

        btn_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, QRActivity.class);
                startActivity(intent);
            }
        });
    }


}





package com.sirs.smartcipher.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
import com.sirs.smartcipher.network.MyHttpClient;

import java.io.IOException;
import java.net.ConnectException;
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
                Date date = new Date();
                SimpleDateFormat df = new SimpleDateFormat("HH:MM:ss");
                tv_log.setText(text +"\n"+ df.format(date)+ ": Connection Started");
                try {

                    if(!active) {
                        active = true;
                        MyHttpClient http = new MyHttpClient(active);
                        http.start();

                    }
                    else {
                        // Do nothing
                        Context context = MyApp.getAppContext();
                        Toast.makeText(context, "Failed connection, please make sure your key is up to date", Toast.LENGTH_SHORT).show();
                        tv_log.setText(text +"\n"+ df.format(date)+ ": Connection Terminated");

                    }
                    return;

                }catch (Exception e) {

                    Log.d(TAG, "Failed connection, please make sure your key is up to date");
                    Toast.makeText(MainActivity.this, "Failed connection, please make sure your key is up to date", Toast.LENGTH_SHORT).show();
                    System.out.println(e.getMessage());


                }
            }
        });

        /*uncommet this to qr code work*/
/*        btn_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, QRActivity.class);
                startActivity(intent);
            }
        });*/

        btn_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    MyBluetoothClient b = new MyBluetoothClient(true);
                    //b.run();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (UnrecoverableEntryException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}





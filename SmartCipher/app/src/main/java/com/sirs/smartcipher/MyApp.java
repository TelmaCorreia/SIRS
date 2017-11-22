package com.sirs.smartcipher;

import android.app.Application;
import android.content.Context;

/**
 * Created by telma on 11/21/2017.
 */

public class MyApp extends Application{


    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApp.context;
    }

}

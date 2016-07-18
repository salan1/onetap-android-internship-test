package com.example.pc.onetapapp.SharedPref;

import android.app.Application;

public class ApplicationSingleton extends Application {

    public static final String TAG = "In ApplicationSingleton";

    private static ApplicationSingleton mInstance;
    private PrefManager pref;

    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized ApplicationSingleton getInstance() {
        return mInstance;
    }

    public PrefManager getPrefManager() {
        if (pref == null) {
            pref = new PrefManager(this);
        }

        return pref;
    }

}

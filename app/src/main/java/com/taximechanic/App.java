package com.taximechanic;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class App extends Application {

    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d("APP", "Terminate");
    }

    static public App getInstance() {
        return mInstance;
    }

    static public Context getContext() {
        return mInstance.getBaseContext();
    }
}

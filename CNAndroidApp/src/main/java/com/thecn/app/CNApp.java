package com.thecn.app;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.testflightapp.lib.TestFlight;
import com.thecn.app.tools.BitmapLruCache;
import com.thecn.app.tools.MyVolley;

public class CNApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {

        //Initialize TestFlight with your app token.
        TestFlight.takeOff(this, "1aef7b8d-65f4-4ddd-9d0a-70c714b46661");

        AppSession.getInstance().Initialize(this);
        BitmapLruCache.setCacheSize(this);
        MyVolley.init(this);
        //MyVolley.trustAllHosts();
    }

    private Activity mCurrentActivity = null;

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }
}

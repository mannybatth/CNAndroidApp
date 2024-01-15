package com.thecn.app.activities.MyActivities;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;

import com.thecn.app.CNApp;

/**
 * Thanks to this post: http://stackoverflow.com/a/13994622 for the idea to
 * keep a reference to the current activity
 */
public class MyActionBarActivity extends ActionBarActivity {

    @Override
    protected void onResume() {
        super.onResume();
        getCNApp().setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences() {
        CNApp app = getCNApp();
        Activity activity = app.getCurrentActivity();
        if (activity != null && activity.equals(this)) {
            app.setCurrentActivity(null);
        }
    }

    protected CNApp getCNApp() {
        return (CNApp) getApplicationContext();
    }
}

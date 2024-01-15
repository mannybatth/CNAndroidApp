package com.thecn.app.activities;

import android.content.Intent;
import android.os.Bundle;

import com.thecn.app.AppSession;
import com.thecn.app.activities.MyActivities.MyFragmentActivity;
import com.thecn.app.models.User.User;

public class LauncherActivity extends MyFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppSession session = AppSession.getInstance();
        Intent intent;

        String token = session.getToken();
        User user = session.getUser();
        if (token != null && user != null) {
            intent = new Intent(this, HomeFeedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        startActivity(intent);
        finish();
    }

}

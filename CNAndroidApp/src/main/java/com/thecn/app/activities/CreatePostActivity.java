package com.thecn.app.activities;

import android.os.Bundle;

import com.thecn.app.R;
import com.thecn.app.activities.MyActivities.MyFragmentActivity;

public class CreatePostActivity extends MyFragmentActivity {

    private static final String TAG = CreatePostActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
    }
}
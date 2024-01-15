package com.thecn.app.activities;

import android.os.Bundle;

import com.thecn.app.R;
import com.thecn.app.fragments.HomeFeedFragment;

public class HomeFeedActivity extends NavigationActivity {

    private static final String TAG = HomeFeedActivity.class.getSimpleName();

    private HomeFeedFragment homeFeedFragment;
    private String fragmentTag = "HOME_FEED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarAndTitle("Home Feed");

        if (savedInstanceState == null) {
            hideProgressBar();
            homeFeedFragment = new HomeFeedFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, homeFeedFragment, fragmentTag)
                    .commit();
        } else {
            hideProgressBar();
            homeFeedFragment = (HomeFeedFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        }
    }

    @Override
    public void openHomeFeedPage() {
        if (homeFeedFragment != null) {
            homeFeedFragment.refresh();
            homeFeedFragment.showPostButton();
        }
    }
}

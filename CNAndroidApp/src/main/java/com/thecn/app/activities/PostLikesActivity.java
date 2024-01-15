package com.thecn.app.activities;

import android.os.Bundle;

import com.thecn.app.R;
import com.thecn.app.fragments.PostLikesFragment;
import com.thecn.app.models.Post;

public class PostLikesActivity extends NavigationActivity {

    private static final String TAG = PostLikesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarAndTitle("Post Likes");

        Post post = (Post) getIntent().getSerializableExtra("post");

        hideProgressBar();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, PostLikesFragment.newInstance(post))
                    .commit();
        }
    }
}

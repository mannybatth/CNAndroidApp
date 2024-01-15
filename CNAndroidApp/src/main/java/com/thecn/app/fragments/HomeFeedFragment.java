package com.thecn.app.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thecn.app.R;
import com.thecn.app.models.Post;
import com.thecn.app.stores.PostStore;

public class HomeFeedFragment extends BasePostListFragment {

    public static final String TAG = HomeFeedFragment.class.getSimpleName();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addPostButton(R.id.post_button);
        int buttonHeight = (int) getResources().getDimension(R.dimen.post_button_height);
        getListView().setPadding(0, buttonHeight, 0 , 0);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_list, null);
    }

    public void loadPosts() {
        PostStore.getHomePostsWithFilter("new_posts", getLimit(), getOffset(), new PostsCallback(getHandler()));
    }

    public String getFragmentID() {
        return TAG;
    }

    @Override
    public void onResume() {
        super.onResume();
        showPostButton();
    }

    @Override
    public void onPostDeleted(Post post) {
        super.onPostDeleted(post);
        showPostButton();
    }
}

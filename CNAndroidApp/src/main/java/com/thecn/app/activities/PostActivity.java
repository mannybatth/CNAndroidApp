package com.thecn.app.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.fragments.PostFragment;
import com.thecn.app.models.Post;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;

import org.json.JSONObject;

public class PostActivity extends NavigationActivity {

    private Post mPost;

    private static final String mLoadPostFragmentTag = "load_post";
    public static final String FRAGMENT_BUNDLE_KEY = "pr_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OBS", "on create");

        setActionBarAndTitle("Post");

        if (savedInstanceState == null) {
            String postId;
            try {
                mPost = (Post) getIntent().getSerializableExtra("post");
                postId = mPost.getId();
                if (postId == null) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException e) {
                onLoadingError();
                return;
            }

            final boolean textFocus = getIntent().getBooleanExtra("textFocus", false);

            LoadPostFragment fragment = new LoadPostFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(fragment, mLoadPostFragmentTag)
                    .commit();

            fragment.loadPost(postId, textFocus);

        } else {
            LoadPostFragment fragment =
                    (LoadPostFragment) getSupportFragmentManager().findFragmentByTag(mLoadPostFragmentTag);

            if (!fragment.loading) {
                mPost = (Post) savedInstanceState.getSerializable("post");
            }

            hideProgressBar();
        }
    }

    public static class LoadPostFragment extends MyFragment {

        public boolean loading = false;

        public void loadPost(String postID, final boolean textFocus) {
            loading = true;

            PostStore.getPostById(postID, new ResponseCallback(getHandler()) {
                @Override
                public void onPreExecute() {
                    loading = false;
                }

                @Override
                public void onSuccess(JSONObject response) {
                    Post post = PostStore.getData(response);
                    PostActivity activity = getPostActivity();

                    if (post != null) {
                        activity.setPost(post);
                        activity.hideProgressBar();

                        Fragment fragment = PostFragment.newInstance(post, textFocus);
                        activity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, fragment, PostActivity.FRAGMENT_BUNDLE_KEY)
                                .commit();
                    } else {
                        activity.onLoadingError();
                    }
                }

                @Override
                public void onFailure(JSONObject response) {
                    getPostActivity().onLoadingError();
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
                    getActivity().finish();
                }
            });
        }

        private PostActivity getPostActivity() {
            return (PostActivity) getActivity();
        }
    }

    private void onLoadingError() {
        AppSession.showDataLoadError("post");
        finish();
    }

    public void setPost(Post post) {
        mPost = post;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("post", mPost);
    }

    @Override
    public void openPostPage(Post post, boolean textFocus) {
        // override to prevent another reflections page from opening
        if (post.getId().equals(mPost.getId())) {
            if (textFocus) {
                try {
                    PostFragment fragment =
                            (PostFragment) getSupportFragmentManager()
                                    .findFragmentByTag(FRAGMENT_BUNDLE_KEY);

                    fragment.focusReflectionTextBox();
                } catch (NullPointerException e) {
                    // something went wrong
                } catch (ClassCastException e) {
                    // something went wrong
                }
            } else {
                closeNotificationDrawer();
            }
        } else {
            super.openPostPage(post, textFocus);
        }
    }
}

package com.thecn.app.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.thecn.app.AppSession;
import com.thecn.app.fragments.Conexus.ConexusPostsFragment;
import com.thecn.app.fragments.Course.CoursePostsFragment;
import com.thecn.app.fragments.HomeFeedFragment;
import com.thecn.app.fragments.Profile.ProfilePostsFragment;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.Post;

/**
 * Created by philjay on 6/4/14.
 */
public class PostChangeHandler {

    static final int ADD_ACTION = 0;
    static final int UPDATE_ACTION = 1;
    static final int DELETE_ACTION = 2;

    static final String INTENT_FILTER = "on_post_change";

    Listener mListener;
    LocalBroadcastManager mBroadcastManager;
    BroadcastReceiver mBroadcastReceiver;

    public static interface Listener {
        public void onPostUpdated(Post post);
        public void onPostAdded(Post post, String[] ids);
        public void onPostDeleted(Post post);
    }

    public PostChangeHandler(Listener listener, Context context) {
        mListener = listener;

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int action = intent.getIntExtra("action", 99);
                Post post = (Post) intent.getSerializableExtra("post");
                switch (action) {
                    case ADD_ACTION:
                        String[] ids = intent.getStringArrayExtra("ids");
                        mListener.onPostAdded(post, ids);
                        break;

                    case UPDATE_ACTION:
                        mListener.onPostUpdated(post);
                        break;

                    case DELETE_ACTION:
                        mListener.onPostDeleted(post);
                        break;
                    default:
                        break;
                }
            }
        };

        mBroadcastManager = LocalBroadcastManager
                .getInstance(context);
        mBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(INTENT_FILTER));
    }

    public void unregisterReceiver() {
        mBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }

    public static void sendAddedBroadcast(Post post) {
        Context context = AppSession.getInstance().getApplicationContext();

        if (post != null && context != null) {
            String[] courseIDs = null;
            String[] conexusIDs = null;

            try {
                courseIDs = Course.getIds(post.getCourses());
            } catch (NullPointerException e) {/*no data*/}
            
            try {
                conexusIDs = Conexus.getIds(post.getConexuses());
            } catch (NullPointerException e) {/*no data*/}

            int numFragmentIDs = 0;

            if (courseIDs != null) numFragmentIDs += courseIDs.length;
            if (conexusIDs != null) numFragmentIDs += conexusIDs.length;

            String[] fragmentIDs = new String[numFragmentIDs + 2];
            fragmentIDs[0] = HomeFeedFragment.TAG;
            fragmentIDs[1] = ProfilePostsFragment.TAG + AppSession.getInstance().getUser().getId();

            for (int i = 0; i < courseIDs.length; i++) {
                fragmentIDs[i + 2] = CoursePostsFragment.TAG + courseIDs[i];
            }

            for (int i = 0; i < conexusIDs.length; i++) {
                fragmentIDs[i + 2 + courseIDs.length] = ConexusPostsFragment.TAG + conexusIDs[i];
            }

            Intent intent = new Intent(INTENT_FILTER);
            intent.putExtra("action", ADD_ACTION);
            intent.putExtra("post", post);
            intent.putExtra("ids", fragmentIDs);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public static void sendUpdatedBroadcast(Post post) {
        Context context = AppSession.getInstance().getApplicationContext();

        if (post != null && context != null) {
            Intent intent = new Intent(INTENT_FILTER);
            intent.putExtra("action", UPDATE_ACTION);
            intent.putExtra("post", post);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public static void sendDeletedBroadcast(Post post) {
        Context context = AppSession.getInstance().getApplicationContext();

        if (post != null && context != null) {
            Intent intent = new Intent(INTENT_FILTER);
            intent.putExtra("action", DELETE_ACTION);
            intent.putExtra("post", post);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}

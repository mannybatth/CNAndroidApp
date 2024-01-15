package com.thecn.app.fragments.Profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.thecn.app.R;
import com.thecn.app.fragments.BasePostListFragment;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.PostStore;

public class ProfilePostsFragment extends BasePostListFragment {

    public static final String TAG = ProfilePostsFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_USER_KEY = "user";

    private User mUser;

    private View headerView;

    private ProfileHeader mProfileHeader;

    public static ProfilePostsFragment newInstance(User mUser) {
        ProfilePostsFragment fragment = new ProfilePostsFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_USER_KEY, mUser);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = (User) getArguments().getSerializable(FRAGMENT_BUNDLE_USER_KEY);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        headerView = getLayoutInflater(savedInstanceState).inflate(R.layout.profile_header, null);
        getListView().addHeaderView(headerView, null, false);

        mProfileHeader = new ProfileHeader(headerView, mUser, this);
        mProfileHeader.setUpHeader();

        setListViewScrollListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mProfileHeader.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProfileHeader.removeUpdater();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        position --;
        super.onListItemClick(l, v, position, id);
    }

    public void loadPosts() {
        PostStore.getPostsFromUser(mUser.getId(), getLimit(), getOffset(), new PostsCallback(getHandler()));
    }

    public String getFragmentID() {
        return TAG + mUser.getId();
    }
}

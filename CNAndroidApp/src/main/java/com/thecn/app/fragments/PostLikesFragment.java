package com.thecn.app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.NavigationActivity;
import com.thecn.app.adapters.RosterAdapter;
import com.thecn.app.fragments.MyFragments.MyListFragment;
import com.thecn.app.models.Post;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.LoadingViewController;

import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class PostLikesFragment extends MyListFragment implements OnRefreshListener {

    public static final String TAG = PostLikesFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_POST_KEY = "post";

    protected PullToRefreshLayout mPullToRefreshLayout;

    private RosterAdapter mPostLikesAdapter;
    private LoadingViewController mFooter;
    private Post mPost;

    private int limit, offset;
    private boolean loading, noMore;

    public static PostLikesFragment newInstance(Post mPost) {
        PostLikesFragment fragment = new PostLikesFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_POST_KEY, mPost);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        limit = 10;
        offset = 0;
        loading = false;
        noMore = false;

        mPost = (Post) getArguments().getSerializable(FRAGMENT_BUNDLE_POST_KEY);
        mPostLikesAdapter = new RosterAdapter(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewGroup viewGroup = (ViewGroup) view;

        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
        ActionBarPullToRefresh.from(getActivity())
                .options(Options.create()
                        .scrollDistance(.35f)
                        .build())
                .insertLayoutInto(viewGroup)
                .theseChildrenArePullable(getListView(), getListView().getEmptyView())
                .listener(this)
                .setup(mPullToRefreshLayout);

        mPullToRefreshLayout.setEnabled(false);

        setListAdapter(null);

        ListView listView = getListView();
        mFooter = new LoadingViewController(listView, getLayoutInflater(savedInstanceState));
        mFooter.setNoneMessage("There are no likes for this post.");
        listView.setFooterDividersEnabled(false);
        listView.setBackgroundColor(getResources().getColor(R.color.base_listview_background_color));

        setListAdapter(mPostLikesAdapter);

        if (mPostLikesAdapter.getCount() == 0) {

            // Wait until fragment animation is complete
            long duration = getResources().getInteger(R.integer.fragment_anim_duration);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadMoreUsers();
                }
            }, duration);
        }

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
                    loadMoreUsers();
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        getListView().setItemChecked(position, true);

        User user = mPostLikesAdapter.getItem(position);
        ((NavigationActivity) getActivity()).openProfilePage(user);
    }

    public void getUsers() {
        if (!loading && !noMore) {
            loading = true;
            mFooter.setLoading();
            PostStore.getPostLikes(mPost.getId(), limit, offset, new ResponseCallback(getHandler()) {
                @Override
                public void onSuccess(JSONObject response) {
                    ArrayList<User> users = UserStore.getListData(response);

                    if (users != null) {
                        mPostLikesAdapter.addAll(users);
                        int nextOffset = StoreUtil.getNextOffset(response);
                        if (nextOffset != -1) offset = nextOffset;
                    } else {
                        noMore = true;
                    }

                    onUserLoadingComplete();
                }

                @Override
                public void onFailure(JSONObject response) {
                    AppSession.showDataLoadError("user list");
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
                    onUserLoadingComplete();
                }

                @Override
                public void onPostExecute() {
                    onUserLoadingComplete();
                }
            });
        }
    }

    public void onUserLoadingComplete() {
        loading = false;

        if (mPostLikesAdapter.getCount() == 0) mFooter.showNoneMessage();
        else mFooter.clear();
    }

    public void loadMoreUsers() {
        if (!loading && !noMore) {
            getUsers();
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        mPullToRefreshLayout.setRefreshComplete();
    }
}

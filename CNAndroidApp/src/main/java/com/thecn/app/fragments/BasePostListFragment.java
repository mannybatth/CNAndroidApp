package com.thecn.app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.NavigationActivity;
import com.thecn.app.adapters.PostsAdapter;
import com.thecn.app.fragments.MyFragments.MyListFragment;
import com.thecn.app.models.Post;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.LoadingViewController;
import com.thecn.app.views.ObservableListView;
import com.thecn.app.tools.PausingHandler;
import com.thecn.app.tools.PostChangeHandler;
import com.thecn.app.tools.SlidingViewController;

import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public abstract class BasePostListFragment extends MyListFragment implements OnRefreshListener, PostChangeHandler.Listener {

    protected PullToRefreshLayout mPullToRefreshLayout;

    protected PostsAdapter mPostsAdapter;
    protected PostChangeHandler mPostChangeHandler;

    private int limit = 10;
    private int offset = 0;
    private boolean loading = false;
    private boolean noMore = false;

    private long lastRefreshTime = 0;

    private boolean needRefresh = false;

    LoadingViewController mFooter;

    AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {}

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if ((totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
                getPosts();
            }
        }
    };

    public class PostsCallback extends ResponseCallback {
        public PostsCallback(PausingHandler handler) {
            super(handler);
        }

        @Override
        public void onSuccess(JSONObject response) {
            ArrayList<Post> posts = PostStore.getListData(response);
            int nextOffset = StoreUtil.getNextOffset(response);

            addPosts(posts, nextOffset);
        }

        @Override
        public void onFailure(JSONObject response) {
            AppSession.showDataLoadError("post");
            onLoadingComplete();
        }

        @Override
        public void onError(Exception error) {
            StoreUtil.showExceptionMessage(error);
            onLoadingComplete();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPostsAdapter = new PostsAdapter(this);
        mPostChangeHandler = new PostChangeHandler(this, getActivity());
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

        ListView listView = getListView();

        listView.setBackgroundColor(getResources().getColor(R.color.base_listview_background_color));
        listView.setFooterDividersEnabled(false);

        LayoutInflater inflater = getLayoutInflater(savedInstanceState);
        mFooter = new LoadingViewController(listView, inflater);
        mFooter.setNoneMessage("There are no posts here.");

        setListAdapter(mPostsAdapter);
    }

    @Override
    public void onPostUpdated(Post post) {
        new Post.ProcessTask(post, new Post.ProcessCallback() {
            @Override
            public void onProcessComplete(Post post) {
                for (int i = 0; i < mPostsAdapter.getCount(); i++) {
                    if (mPostsAdapter.get(i).getId().equals(post.getId())) {
                        post.setFullView(false);
                        mPostsAdapter.set(i, post);
                        break;
                    }
                }
            }
        }).execute();
    }

    @Override
    public void onPostAdded(Post post, String[] ids) {
        for (String id : ids) {
            if (getFragmentID().equals(id)) {
                new Post.ProcessTask(post, new Post.ProcessCallback() {
                    @Override
                    public void onProcessComplete(Post post) {
                        post.setFullView(false);
                        mPostsAdapter.add(0, post);
                    }
                }).execute();

                break;
            }
        }
    }

    @Override
    public void onPostDeleted(Post post) {
        for (int i = 0; i < mPostsAdapter.getCount(); i++) {
            if (mPostsAdapter.get(i).getId().equals(post.getId())) {
                mPostsAdapter.remove(i);
                AppSession.showToast("Post deleted");
                break;
            }
        }
    };

    @Override
    public void onRefreshStarted(View view) {
        refresh();
    }

    public void refresh() {
        offset = 0;
        mPostsAdapter.clear();
        getPosts();
    }

    public abstract void loadPosts();

    public abstract String getFragmentID();

    protected int getLimit() {
        return limit;
    }

    protected void setLimit(int limit) {
        this.limit = limit;
    }

    protected int getOffset() {
        return offset;
    }

    View mPostButton, mTwinPostButton;

    public View getPostButton() {
        return mPostButton;
    }

    public void showPostButton() {
        mPostButton.post(new Runnable() {
            @Override
            public void run() {
                mPostButton.setTranslationY(0);
            }
        });
    }

    public View getTwinPostButton() {
        return mTwinPostButton;
    }

    protected void addPostButton(Integer hoverButtonID) {
        addPostButton(hoverButtonID, null);
    }

    protected void addPostButton(Integer hoverButtonID, Integer twinButtonID) {
        ObservableListView lv = (ObservableListView) getListView();

        SlidingViewController controller = new SlidingViewController(lv);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushCreatePostActivity();
            }
        };

        if (hoverButtonID != null) {
            mPostButton = getView().findViewById(hoverButtonID);
            if (mPostButton != null) {
                mPostButton.findViewById(R.id.actual_post_button).setOnClickListener(listener);
                controller.setSlidingView(mPostButton);
            }
        }

        if (twinButtonID != null) {
            mTwinPostButton = getView().findViewById(twinButtonID);
            if (mTwinPostButton != null){
                mTwinPostButton.findViewById(R.id.actual_post_button).setOnClickListener(listener);
                controller.setTwinView(mTwinPostButton);
            }
        }

        controller.setOnScrollListener(mScrollListener);
    }

    protected void setListViewScrollListener() {
        getListView().setOnScrollListener(mScrollListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        needRefresh = System.currentTimeMillis() - lastRefreshTime > 1800000;
        if (needRefresh) refresh();
        else if (loading) {
            mFooter.setLoading();
        }
    }

    protected void getPosts() {
        if (!loading && !noMore) {
            loading = true;
            mFooter.setLoading();
            lastRefreshTime = System.currentTimeMillis();
            loadPosts();
        }
    }

    protected void addPosts(ArrayList<Post> posts, final int nextOffset) {
        if (posts != null) {

            new Post.ProcessListTask(posts, new Post.ProcessListCallback() {
                @Override
                public void onProcessComplete(ArrayList<Post> posts) {
                    if (mPostsAdapter != null) {
                        mPostsAdapter.addAll(posts);
                        if (offset != -1) offset = nextOffset;
                        onLoadingComplete();
                    }
                }
            }).execute();

        } else {
            noMore = true;
            onLoadingComplete();
        }
    }

    protected void onLoadingComplete() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loading = false;
                mPullToRefreshLayout.setRefreshComplete();
            }
        }, 500);

        needRefresh = false;

        if (mPostsAdapter.getCount() == 0) mFooter.showNoneMessage();
        else mFooter.clear();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);

        Post post = mPostsAdapter.getItem(position);
        ((NavigationActivity) getActivity()).openPollPage(post);//.openPostPage(post, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        getListView().setDivider(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPostChangeHandler.unregisterReceiver();
    }
}

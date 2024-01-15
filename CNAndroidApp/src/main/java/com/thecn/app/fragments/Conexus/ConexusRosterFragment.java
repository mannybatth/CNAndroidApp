package com.thecn.app.fragments.Conexus;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.NavigationActivity;
import com.thecn.app.adapters.RosterAdapter;
import com.thecn.app.fragments.MyFragments.MyListFragment;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.ConexusStore;
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

public class ConexusRosterFragment extends MyListFragment implements OnRefreshListener {

    public static final String TAG = ConexusRosterFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_CONEXUS_KEY = "conexus";

    private Conexus mConexus;

    protected PullToRefreshLayout mPullToRefreshLayout;

    private RosterAdapter mRosterAdapter;

    private int limit, offset;
    private boolean loading, noMore;

    private LoadingViewController mFooter;

    public static ConexusRosterFragment newInstance(Conexus mConexus) {
        ConexusRosterFragment fragment = new ConexusRosterFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_CONEXUS_KEY, mConexus);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        limit = 30;
        offset = 0;
        loading = false;
        noMore = false;

        mConexus = (Conexus) getArguments().getSerializable(FRAGMENT_BUNDLE_CONEXUS_KEY);
        mRosterAdapter = new RosterAdapter(this);
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

        //mPullToRefreshLayout.setEnabled(false);

        setListAdapter(null);

        ListView listView = getListView();
        mFooter = new LoadingViewController(listView, getLayoutInflater(savedInstanceState));
        listView.setFooterDividersEnabled(false);
        listView.setBackgroundColor(getResources().getColor(R.color.base_listview_background_color));

        setListAdapter(mRosterAdapter);

        if (mRosterAdapter.getCount() == 0) {
            getUsers();
        }

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {}

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
                    getUsers();
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        getListView().setItemChecked(position, true);

        User user = mRosterAdapter.getItem(position);
        ((NavigationActivity) getActivity()).openProfilePage(user);
    }

    public void getUsers() {
        if (!loading && !noMore) {
            loading = true;
            mFooter.setLoading();
            ConexusStore.getConexusRoster(mConexus, limit, offset, new ResponseCallback(getHandler()) {
                @Override
                public void onSuccess(JSONObject response) {
                    ArrayList<User> users = UserStore.getRosterData(response);

                    if (users != null) {
                        mRosterAdapter.addAll(users);
                        int nextOffset = StoreUtil.getNextOffset(response);
                        if (nextOffset != -1) offset = nextOffset;
                    } else {
                        noMore = true;
                    }
                }

                @Override
                public void onFailure(JSONObject response) {
                    AppSession.showDataLoadError("user list");
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
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

        mFooter.clear();
        mPullToRefreshLayout.setRefreshComplete();
    }

    @Override
    public void onRefreshStarted(View view) {
        offset = 0;
        getUsers();
    }
}

package com.thecn.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.NavigationActivity;
import com.thecn.app.adapters.NotificationAdapters.ColleagueRequestAdapter;
import com.thecn.app.models.ColleagueRequest;
import com.thecn.app.models.UserNewMessage;
import com.thecn.app.stores.ColleagueRequestStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.LoadingViewController;

import org.json.JSONObject;

import java.util.ArrayList;

public class ColleagueRequestFragment extends BaseNotificationFragment {

    public static final String TAG = NotificationFragment.class.getSimpleName();

    private ColleagueRequestAdapter mAdapter;
    private LoadingViewController mFooter;

    private int limit, offset;
    private boolean loading, noMore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        limit = 20;
        offset = 0;
        loading = false;
        noMore = false;

        mAdapter = new ColleagueRequestAdapter(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_colleague_requests, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(null);

        ListView listView = getListView();
        mFooter = new LoadingViewController(listView, getLayoutInflater(savedInstanceState));
        mFooter.setNoneMessage("There are no new colleague requests.");
        listView.setFooterDividersEnabled(false);

        setListAdapter(mAdapter);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean shouldLoad = hasBeenViewed() && ((totalItemCount - visibleItemCount) <= firstVisibleItem);
                if (shouldLoad) {
                    getData();
                }
            }
        });
    }

    public void emptyList() {
        mAdapter.clear();
        offset = 0;
        noMore = false;
    }


    public void getData() {
        if (!loading && !noMore) {
            loading = true;
            mFooter.setLoading();
            ColleagueRequestStore.getRequests(limit, offset, new ResponseCallback(getHandler()) {
                @Override
                public void onSuccess(JSONObject response) {
                    ArrayList<ColleagueRequest> requests = ColleagueRequestStore.getListData(response);

                    if (requests != null) {
                        mAdapter.addAll(requests);
                        int nextOffset = StoreUtil.getNextOffset(response);
                        if (nextOffset != -1) offset = nextOffset;
                    } else {
                        noMore = true;
                    }
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
                }

                @Override
                public void onPostExecute() {
                    onLoadingComplete();
                }
            });
        }
    }

    public boolean hasNewData() {
        UserNewMessage userNewMessage = AppSession.getInstance().getUserNewMessage();

        return userNewMessage.getColleagueRequestCount() > 0;
    }

    public void onLoadingComplete() {
        loading = false;

        setRequestDisplayZero();

        if (mAdapter.getCount() == 0) mFooter.showNoneMessage();
        else mFooter.clear();
    }

    void setRequestDisplayZero() {
        UserNewMessage userNewMessage = AppSession.getInstance().getUserNewMessage();
        userNewMessage.setColleagueRequestCount(0);
        AppSession.getInstance().setUserNewMessage(userNewMessage);

        ((NavigationActivity) getActivity()).setAllNotificationDisplays();
    }
}

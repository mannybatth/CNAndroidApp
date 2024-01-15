package com.thecn.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.NavigationActivity;
import com.thecn.app.adapters.NotificationAdapters.EmailNotificationAdapter;
import com.thecn.app.models.Email;
import com.thecn.app.models.UserNewMessage;
import com.thecn.app.stores.EmailStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.LoadingViewController;

import org.json.JSONObject;

import java.util.ArrayList;

public class EmailNotificationFragment extends BaseNotificationFragment {

    public static final String TAG = EmailFragment.class.getSimpleName();

    private EmailNotificationAdapter mAdapter;
    private LoadingViewController mFooter;

    private int limit, offset;
    private boolean loading, noMore;

    private Button composeEmailButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        limit = 20;
        offset = 0;
        loading = false;
        noMore = false;

        mAdapter = new EmailNotificationAdapter(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        composeEmailButton = (Button) inflater.inflate(R.layout.compose_email_button, null, false);
        return inflater.inflate(R.layout.fragment_email_notifications, container, false);
    }

    private NavigationActivity getBaseActivity() {
        return (NavigationActivity) getActivity();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(null);

        ListView listView = getListView();

        listView.addHeaderView(composeEmailButton);

        composeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBaseActivity().pushComposeEmailActivity();
            }
        });

        mFooter = new LoadingViewController(listView, getLayoutInflater(savedInstanceState));
        mFooter.setNoneMessage("You have no emails.");
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        position--;
        super.onListItemClick(l, v, position, id);

        Email email = mAdapter.getItem(position);
        EmailStore.markEmailRead(email.getId());
        getBaseActivity().openEmailPage(email);
    }

    public void getData() {
        if (!loading && !noMore) {
            loading = true;
            mFooter.setLoading();
            EmailStore.getEmails(limit, offset, new ResponseCallback(getHandler()) {
                @Override
                public void onSuccess(JSONObject response) {
                    ArrayList<Email> emails = EmailStore.getListData(response);

                    if (emails != null) {
                        mAdapter.addAll(emails);
                        int nextOffset = StoreUtil.getNextOffset(response);
                        if (nextOffset != -1) offset = nextOffset;
                    } else {
                        noMore = true;
                    }
                }

                @Override
                public void onFailure(JSONObject response) {
                    AppSession.showDataLoadError("email");
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

        return userNewMessage.getEmailCount() > 0;
    }

    public void onLoadingComplete() {
        loading = false;

        setEmailDisplayZero();

        if (mAdapter.getCount() == 0) mFooter.showNoneMessage();
        else mFooter.clear();
    }

    private void setEmailDisplayZero() {
        UserNewMessage userNewMessage = AppSession.getInstance().getUserNewMessage();
        userNewMessage.setEmailCount(0);
        AppSession.getInstance().setUserNewMessage(userNewMessage);

        ((NavigationActivity) getActivity()).setAllNotificationDisplays();
    }

}

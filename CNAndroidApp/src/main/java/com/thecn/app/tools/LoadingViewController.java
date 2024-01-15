package com.thecn.app.tools;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.thecn.app.R;

/**
 * Created by philjay on 6/4/14.
 */
public class LoadingViewController {

    private ListView mListView;
    private View mView;

    private View mLoadingView;
    private TextView mNoneMessageView;
    private TextView mEndMessageView;

    private boolean isAdded = false;

    public LoadingViewController(LayoutInflater inflater) {
        mView = inflater.inflate(R.layout.loading_view, null, false);
        mLoadingView = mView.findViewById(R.id.progressBar);
        mNoneMessageView = (TextView) mView.findViewById(R.id.none_message);
        setNoneMessage("There is nothing here...");
        mEndMessageView = (TextView) mView.findViewById(R.id.end_message);
        setEndMessage("End of list");
    }

    public LoadingViewController(ListView listView, LayoutInflater inflater) {
        mListView = listView;

        mView = inflater.inflate(R.layout.loading_view, listView, false);
        mLoadingView = mView.findViewById(R.id.progressBar);
        mNoneMessageView = (TextView) mView.findViewById(R.id.none_message);
        setNoneMessage("There is nothing here...");
        mEndMessageView = (TextView) mView.findViewById(R.id.end_message);
        setEndMessage("End of list");

        add();
    }

    public View getView() {
        return mView;
    }

    public View getLoadingView() {
        return mLoadingView;
    }

    public TextView getNoneMessageView() {
        return mNoneMessageView;
    }

    public TextView getEndMessageView() {
        return mEndMessageView;
    }

    public void setLoading() {
        showOneHideOthers(mLoadingView);
    }

    public void showNoneMessage() {
        showOneHideOthers(mNoneMessageView);
    }

    public void showEndMessage() {
        showOneHideOthers(mEndMessageView);
    }

    private void showOneHideOthers(View showView) {
        add();

        int loadingViewVisibility = showView == mLoadingView ? View.VISIBLE : View.INVISIBLE;
        int noneMessageVisibility = showView == mNoneMessageView ? View.VISIBLE : View.INVISIBLE;
        int endMessageVisibility = showView == mEndMessageView ? View.VISIBLE : View.INVISIBLE;

        mLoadingView.setVisibility(loadingViewVisibility);
        mNoneMessageView.setVisibility(noneMessageVisibility);
        mEndMessageView.setVisibility(endMessageVisibility);

        mView.setVisibility(View.VISIBLE);
    }

    public void add() {
        if (isAdded || mListView == null) return;

        mListView.addFooterView(mView);
        isAdded = true;
    }

    public void remove() {
        if (mListView == null) return;

        if (isAdded) {
            mListView.removeFooterView(mView);
            isAdded = false;
        }
    }

    public void clear() {
        mView.setVisibility(View.GONE);
    }

    public void setNoneMessage(String message) {
        mNoneMessageView.setText(message);
    }

    public void setEndMessage(String message) {
        mEndMessageView.setText(message);
    }
}

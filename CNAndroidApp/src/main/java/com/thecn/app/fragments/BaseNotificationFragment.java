package com.thecn.app.fragments;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.thecn.app.R;
import com.thecn.app.fragments.MyFragments.MyListFragment;

/**
 * Created by philjay on 5/2/14.
 */
public abstract class BaseNotificationFragment extends MyListFragment {

    private boolean hasBeenViewed;

    public void onFocus() {

        boolean refresh;

        if (!hasBeenViewed) {
            refresh = hasBeenViewed = true;
        } else {
            refresh = hasNewData();
        }

        if (refresh) {
            emptyList();
            getData();
        }
    }

    public void onMenuClose() {
        if (hasBeenViewed) {
            hasBeenViewed = false;
            emptyList();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);

        ListView listView = getListView();
        Resources r = getResources();
        listView.setDivider(new ColorDrawable(r.getColor(R.color.base_listview_background_color)));
        listView.setDividerHeight((int) getResources().getDimension(R.dimen.list_divider_height));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onMenuClose();
    }

    public boolean hasBeenViewed() {
        return hasBeenViewed;
    }

    public abstract boolean hasNewData();

    public abstract void getData();

    public abstract void emptyList();
}

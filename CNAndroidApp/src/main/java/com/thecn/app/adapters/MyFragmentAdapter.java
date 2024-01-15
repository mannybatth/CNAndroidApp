package com.thecn.app.adapters;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.thecn.app.activities.NavigationActivity;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.fragments.MyFragments.MyFragmentInterface;
import com.thecn.app.fragments.MyFragments.MyListFragment;
import com.thecn.app.tools.PausingHandler;

/**
 * Created by philjay on 7/10/14.
 */
public abstract class MyFragmentAdapter extends BaseAdapter {
    private MyFragmentInterface mFragmentInterface;

    public MyFragmentAdapter(MyFragmentInterface myFragmentInterface) {
        if (!(myFragmentInterface instanceof MyFragment || myFragmentInterface instanceof MyListFragment)) {
            throw new IllegalStateException("Fragment must be an instance of MyFragment or MyListFragment");
        }

        mFragmentInterface = myFragmentInterface;
    }

    protected MyFragment getMyFragment() {
        return (MyFragment) mFragmentInterface;
    }

    protected MyListFragment getMyListFragment() {
        return (MyListFragment) mFragmentInterface;
    }

    protected LayoutInflater getLayoutInflater() {
        return ((Fragment) mFragmentInterface).getActivity().getLayoutInflater();
    }

    protected Activity getActivity() {
        return ((Fragment) mFragmentInterface).getActivity();
    }

    protected NavigationActivity getNavigationActivity() {
        return (NavigationActivity) getActivity();
    }

    protected PausingHandler getHandler() {
        return mFragmentInterface.getHandler();
    }
}

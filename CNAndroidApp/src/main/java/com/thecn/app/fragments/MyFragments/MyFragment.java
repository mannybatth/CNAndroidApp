package com.thecn.app.fragments.MyFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.thecn.app.tools.PausingHandler;

/**
 * Created by philjay on 7/7/14.
 */
public class MyFragment extends Fragment implements MyFragmentInterface {

    private PausingHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mHandler = new PausingHandler();
    }

    @Override
    public void onPause() {
        mHandler.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.resume();
    }

    public PausingHandler getHandler() {
        return mHandler;
    }
}

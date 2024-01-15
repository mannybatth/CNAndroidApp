package com.thecn.app.tools;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;

import com.thecn.app.views.ObservableListView;

/**
 * Created by philjay on 5/13/14.
 */
public class SlidingViewController {

    private View mView, mTwinView; //mTwinView is shown when on screen (when set), otherwise mView shown
    private ObservableListView mListView;

    private int mListViewPositionY = 0;

    private ObjectAnimator mViewAnimator;

    //1: master listener 2: user specified listener 3: twin scroll listener
    private AbsListView.OnScrollListener[] mListeners = new AbsListView.OnScrollListener[3];

    private ViewTreeObserver.OnGlobalLayoutListener mLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (mListView != null) {
                int[] loc = new int[2];
                mListView.getLocationInWindow(loc);
                mListViewPositionY = loc[1];
            }
        }
    };

    public SlidingViewController(ObservableListView listView) {
        mListView = listView;

        mListView.getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);

        setListener();

        mListeners[0] = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (i == SCROLL_STATE_IDLE) {
                    if (mView != null) {

                        if (mView.getTranslationY() > -mView.getHeight()) {
                            mViewAnimator = ObjectAnimator.ofFloat(mView, "translationY", 0);
                            mViewAnimator.start();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            }
        };
    }

    public void setViewTranslation(float translation) {
        mView.setTranslationY(translation);
    }

    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            for (AbsListView.OnScrollListener listener : mListeners) {
                if (listener != null) {
                    listener.onScrollStateChanged(absListView, i);
                }
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            for (AbsListView.OnScrollListener listener : mListeners) {
                if (listener != null) {
                    listener.onScroll(absListView, i, i2, i3);
                }
            }
        }
    };

    private void setListener() {
        mListView.setOnScrollListener(mScrollListener);
    }

    public SlidingViewController setSlidingView(View view) {

        if (view != null) {

            mView = view;

            mListView.setObserver(new ObservableListView.ListViewObserver() {
                @Override
                public void onScroll(float deltaY) {

                    if (mViewAnimator != null) {
                        mViewAnimator.cancel();
                    }

                    if (mView != null) {
                        int mButtonHeight = mView.getHeight();
                        float newTranslationY = mView.getTranslationY() + deltaY;

                        if (newTranslationY > 0) {
                            mView.setTranslationY(0);
                        } else if (newTranslationY < -mButtonHeight) {
                            mView.setTranslationY(-mButtonHeight);
                        } else {
                            mView.setTranslationY(newTranslationY);
                        }
                    }
                }
            });
        }

        return this;
    }

    public SlidingViewController setTwinView(View twinView) {
        if (mView != null && twinView != null) {
            mTwinView = twinView;
            setViewVisibility();

            mListeners[2] = new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                    setViewVisibility();
                }
            };
        } else {
            mListeners[2] = null;
        }

        return this;
    }

    private int getY(View view) {
        int[] viewCoordinates = new int[2];
        view.getLocationInWindow(viewCoordinates);

        return viewCoordinates[1] - mListViewPositionY;
    }

    private void setViewVisibility() {

        int mTwinViewY = getY(mTwinView);
        int mViewY = getY(mView);

        boolean hideMView = mTwinViewY >= mViewY;

        boolean viewVisible = mView.getVisibility() == View.VISIBLE;

        if (hideMView) {
            //don't hide view if was already hidden
            if (viewVisible) {
                mView.setVisibility(View.INVISIBLE);
            }
        } else if (!viewVisible) { //don't show view if already visible
            mView.setVisibility(View.VISIBLE);
        }
    }

    public void setOnScrollListener(AbsListView.OnScrollListener scrollListener) {
        mListeners[1] = scrollListener;
    }
}
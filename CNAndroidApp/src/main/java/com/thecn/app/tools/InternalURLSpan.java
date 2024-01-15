package com.thecn.app.tools;

import android.text.style.ClickableSpan;
import android.view.View;

/**
* Created by philjay on 7/10/14.
*/ /* source:
  http://blog.elsdoerfer.name/2009/10/29/clickable-urls-in-android-textviews/
* */
public class InternalURLSpan extends ClickableSpan {
    View.OnClickListener mListener;
    private boolean clickActionWorking;

    public InternalURLSpan() {
        clickActionWorking = false;
    }

    public InternalURLSpan(View.OnClickListener listener) {
        mListener = listener;
        clickActionWorking = false;
    }

    //use this to prevent duplicate clicks

    public boolean isClickActionWorking() {
        return clickActionWorking;
    }

    public void setClickActionWorking(boolean working) {
        clickActionWorking = working;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mListener = listener;
    }

    public View.OnClickListener getOnClickListener() {
        return mListener;
    }

    @Override
    public void onClick(View widget) throws NullPointerException {
        if (mListener != null)
            mListener.onClick(widget);
        else
            throw new NullPointerException("OnClickListener not instantiated");
    }
}

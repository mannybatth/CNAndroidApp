package com.thecn.app.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

/**
 * Created by philjay on 5/30/14.
 */
public class ColorFilterButton extends ImageButton {

    public ColorFilterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (isEnabled()) {
            if (action == MotionEvent.ACTION_DOWN) {
                setColorFilter(Color.argb(150, 40, 40, 40));
            } else {
                boolean reset = action == MotionEvent.ACTION_UP ||
                        action == MotionEvent.ACTION_CANCEL;

                if (reset) setColorFilter(null);
            }
        }

        return super.onTouchEvent(event);
    }

}

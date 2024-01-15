package com.thecn.app.views;

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * View created to match the SlidingTabStrip view
 * Used SlidingTabLayout view as a reference when coding this class
 */
public class TabTitleTextView extends TextView {

    private static final byte DEFAULT_DIVIDER_COLOR_ALPHA = 0x20;
    private static final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 2;

    private int mBottomBorderThickness;
    private Paint mBottomBorderPaint;

    public TabTitleTextView(Context context) {
        this(context, null);
    }

    public TabTitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        final float density = getResources().getDisplayMetrics().density;

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorForeground, outValue, true);
        final int themeForegroundColor = outValue.data;

        int bottomBorderColor = Color.argb(
                DEFAULT_DIVIDER_COLOR_ALPHA,
                Color.red(themeForegroundColor),
                Color.green(themeForegroundColor),
                Color.blue(themeForegroundColor)
        );

        mBottomBorderThickness = (int) (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density);
        mBottomBorderPaint = new Paint();
        mBottomBorderPaint.setColor(bottomBorderColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int height = getHeight();
        final int width = getWidth();

        canvas.drawRect(0, height - mBottomBorderThickness, width, height, mBottomBorderPaint);

        super.onDraw(canvas);
    }
}

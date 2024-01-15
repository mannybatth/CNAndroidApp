package com.thecn.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.ImageView;

public class SquareView {

    public static class SquareImageView extends ImageView {

        public SquareImageView(final Context context) {
            super(context);
        }

        public SquareImageView(final Context context, final AttributeSet attrs) {
            super(context, attrs);
        }

        public SquareImageView(final Context context, final AttributeSet attrs, final int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            setMeasuredDimension(width, width);
        }

        @Override
        protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
            super.onSizeChanged(w, w, oldw, oldh);
        }
    }

    public static class SquareCheckbox extends CheckBox {

        public SquareCheckbox(Context context) {
            super(context);
        }

        public SquareCheckbox(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public SquareCheckbox(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            setMeasuredDimension(width, width);
        }

        @Override
        protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
            super.onSizeChanged(w, w, oldw, oldh);
        }
    }
}

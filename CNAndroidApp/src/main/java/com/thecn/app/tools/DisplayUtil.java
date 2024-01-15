package com.thecn.app.tools;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;

/**
 * Created by philjay on 7/23/14.
 */
public class DisplayUtil {

    public static int getDisplayWidth(Activity activity) {
        if (activity == null) return 0;

        Display display = activity.getWindowManager().getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            display.getSize(size);
            return size.x;
        } else {
            return display.getWidth();
        }
    }

    public static int getDisplayHeight(Activity activity) {
        if (activity == null) return 0;

        Display display = activity.getWindowManager().getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            display.getSize(size);
            return size.y;
        } else {
            return display.getHeight();
        }
    }
}

/**
 * Copyright 2013 Ognyan Bankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thecn.app.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;


public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
    private static BitmapLruCache instance = null;
    private static int cacheSize;

    //should only be used from CNApp
    public static void setCacheSize(Context context) {
        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        cacheSize = 1024 * 1024 * memClass / 8; //use 1/8 available memory
    }

    private BitmapLruCache() {
        super(cacheSize);
    }

    public static BitmapLruCache getInstance() {
        if (instance == null)
            instance = new BitmapLruCache();
        return instance;
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    public Bitmap getBitmap(String key) {
        return get(key);
    }

    public void putBitmap(String key, Bitmap bitmap) {
        if (getBitmap(key) == null)
            put(key, bitmap);
    }
}

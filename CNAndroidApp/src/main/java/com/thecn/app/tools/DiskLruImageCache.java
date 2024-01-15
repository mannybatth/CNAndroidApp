package com.thecn.app.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.thecn.app.AppSession;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* a combination of research from Android Developer's
    Caching Bitmaps tutorial:
    http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
    and Platonius's implementation of Jake Wharton's DiskLruCache:
    http://stackoverflow.com/questions/10185898/using-disklrucache-in-android-4-0-does-not-provide-for-opencache-method
    Many thanks to them!
 */
public class DiskLruImageCache {

    private static final String TAG = DiskLruImageCache.class.getSimpleName();

    private static DiskLruImageCache instance = null;

    private Context mContext;

    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarted;

    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10 MB

    private static final CompressFormat COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int COMPRESS_QUALITY = 70;

    private static final String DISK_CACHE_SUBDIR = "thecn_gallery_thumbnails";

    private static final int APP_VERSION = 11;
    private static final int VALUE_COUNT = 1;

    private static final int IO_BUFFER_SIZE = 8 * 1024;

    private DiskLruImageCache() {
        mContext = AppSession.getInstance().getApplicationContext();
        mDiskCacheStarted = false;
        new InitDiskCacheTask().execute();
    }

    public static DiskLruImageCache getInstance() {
        if (instance == null) {
            instance = new DiskLruImageCache();
        }
        return instance;
    }

    class InitDiskCacheTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            synchronized (mDiskCacheLock) {
                try {
                    File cacheDir = getDiskCacheDir();
                    mDiskLruCache = DiskLruCache.open(cacheDir, APP_VERSION, VALUE_COUNT, DISK_CACHE_SIZE);
                    mDiskCacheStarted = true;
                    mDiskCacheLock.notifyAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public void openIfClosed() {
        if (!mDiskCacheStarted)
            new InitDiskCacheTask().execute();
    }

    public boolean close() {
        synchronized (mDiskCacheLock) {
            try {
                mDiskLruCache.close();
                mDiskCacheStarted = false;
            } catch (IOException e) {
                Log.d(TAG + "Error closing disk cache", e.getMessage());
            }
        }
        return mDiskCacheStarted;
    }

    public boolean put(String key, Bitmap data) {
        DiskLruCache.Editor editor = null;

        synchronized (mDiskCacheLock) {
            while (!mDiskCacheStarted) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                    Log.d(TAG + " ERROR", e.getMessage());
                }
            }

            try {
                editor = mDiskLruCache.edit(key);
                if (editor == null) return false;

                if (writeBitmap(data, editor)) {
                    editor.commit();
                    mDiskLruCache.flush();
                    return true;
                } else editor.abort();
            } catch (IOException e) {
                Log.d(TAG + " ERROR", e.getMessage());
                try {
                    if (editor != null) editor.abort();
                } catch (IOException f) {
                    Log.d(TAG + " ERROR ABORTING EDITOR", f.getMessage());
                }
            }
        }

        return false;
    }

    public boolean containsKey(String key) {

        boolean contained = false;

        synchronized (mDiskCacheLock) {
            DiskLruCache.Snapshot snapshot = null;
            try {
                snapshot = mDiskLruCache.get(key);
                contained = snapshot != null;
            } catch (IOException e) {
                Log.d(TAG + " Error", e.getMessage());
                Log.d(TAG, "Unable to determine if key present.  Return false.");
            } finally {
                if (snapshot != null) snapshot.close();
            }
        }

        return contained;
    }

    //unsynchronized helper used by put method which is synchronized
    private boolean writeBitmap(Bitmap bitmap, DiskLruCache.Editor editor)
            throws IOException, FileNotFoundException {

        OutputStream out = null;
        try {
            out = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
            return bitmap.compress(COMPRESS_FORMAT, COMPRESS_QUALITY, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public Bitmap getBitmap(String key) {

        DiskLruCache.Snapshot snapshot = null;
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            while (!mDiskCacheStarted) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                    Log.d(TAG + " ERROR", e.getMessage());
                }
            }

            if (mDiskLruCache != null) {
                try {
                    snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) return null;

                    final InputStream in = snapshot.getInputStream(0);
                    if (in != null) {
                        final BufferedInputStream bis = new BufferedInputStream(in, IO_BUFFER_SIZE);
                        bitmap = BitmapFactory.decodeStream(bis);
                    }
                } catch (IOException e) {
                    Log.d(TAG + " ERROR", e.getMessage());
                } finally {
                    if (snapshot != null) snapshot.close();
                }
            }
        }

        return bitmap;
    }

    // Creates unique subdirectory.
    // Tries to use external storage but will use internal if necessary.
    public File getDiskCacheDir() {
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ?
                        mContext.getExternalCacheDir().getPath() :
                        mContext.getCacheDir().getPath();

        return new File(cachePath + File.separator + DISK_CACHE_SUBDIR);
    }
}

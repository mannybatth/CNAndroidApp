package com.thecn.app.adapters.ThumbnailAdapters;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.tools.BitmapLruCache;
import com.thecn.app.tools.BitmapUtil;
import com.thecn.app.tools.BitmapUtil.AsyncDrawable;
import com.thecn.app.tools.BitmapUtil.BitmapWorkerTask;
import com.thecn.app.tools.DiskLruImageCache;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public abstract class BaseThumbnailAdapter extends BaseAdapter {

    private BitmapLruCache mMemCache;
    private DiskLruImageCache mDiskCache;

    private Bitmap mPlaceHolderBitmap;
    private int dimension;

    private Fragment mFragment;

    public static class ImagePackage {
        private String filePath;
        private String md5Hex;

        public ImagePackage(String filePath) {
            setFilePath(filePath);
            setMd5Hex(new String(Hex.encodeHex(DigestUtils.md5(filePath))));
            //unique hex generated from filepath
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getMd5Hex() {
            return md5Hex;
        }

        public void setMd5Hex(String md5Hex) {
            this.md5Hex = md5Hex;
        }
    }

    public BaseThumbnailAdapter(Fragment fragment) {
        mFragment = fragment;
        Resources r = fragment.getResources();
        dimension = getDimension();
        mPlaceHolderBitmap =
                BitmapUtil.decodeSampledBitmapFromResource(r, R.drawable.blank_image, dimension, dimension);

        mMemCache = BitmapLruCache.getInstance();
        mDiskCache = DiskLruImageCache.getInstance();
    }

    public Fragment getFragment() {
        return mFragment;
    }

    public LayoutInflater getLayoutInflater() {
        return mFragment.getActivity().getLayoutInflater();
    }

    public abstract void remove(ImagePackage imagePackage);

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected void loadBitmap(ImagePackage imgPkg, ImageView imageView) {
        final String filePath = imgPkg.getFilePath();
        final String md5Hex = imgPkg.getMd5Hex();
        final Bitmap bitmap = mMemCache.getBitmap(md5Hex);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else if (BitmapUtil.cancelPotentialWork(md5Hex, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(this, imgPkg,
                    imageView, dimension);

            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mFragment.getResources(), mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);

            task.execute(filePath);
        }
    }

    public void openDiskCacheIfClosed() {
        mDiskCache.openIfClosed();
    }

    public boolean closeDiskCache() {
        return mDiskCache.close();
    }

    private static int getDimension() {
        int dimension;

        Resources resources = AppSession.getInstance()
                .getApplicationContext().getResources();

        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        int orientation = resources.getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            dimension = displayMetrics.heightPixels;
        else dimension = displayMetrics.widthPixels;

        return dimension / 4;
    }
}

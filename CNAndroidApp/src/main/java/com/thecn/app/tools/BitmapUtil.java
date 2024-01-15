package com.thecn.app.tools;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.thecn.app.adapters.ThumbnailAdapters.BaseThumbnailAdapter;

import java.io.IOException;
import java.lang.ref.WeakReference;

/*Methods from developer.android.com bitmap tutorials*/

public class BitmapUtil {

    public static Bitmap decodeSampledBitmapFromFilePath(String filePath,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }


    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private static final String TAG = BitmapWorkerTask.class.getSimpleName();

        private final WeakReference<ImageView> imageViewReference;
        String mFilePath;
        String mMd5Hex;
        int mDimension;
        BitmapLruCache mMemCache;
        DiskLruImageCache mDiskCache;

        BaseThumbnailAdapter.ImagePackage mImgPkg;
        BaseThumbnailAdapter mAdapter;

        public BitmapWorkerTask(BaseThumbnailAdapter adapter,
                                BaseThumbnailAdapter.ImagePackage imgPkg, ImageView imageView, int dimension) {

            // Using WeakReference ensures ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            mFilePath = imgPkg.getFilePath();
            mMd5Hex = imgPkg.getMd5Hex();
            mDimension = dimension;
            mMemCache = BitmapLruCache.getInstance();
            mDiskCache = DiskLruImageCache.getInstance();

            mImgPkg = imgPkg;
            mAdapter = adapter;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = mDiskCache.getBitmap(mMd5Hex);
            if (bitmap == null) {
                try {
                    bitmap = decodeSampledBitmapFromFilePath(mFilePath, mDimension, mDimension);

                    ExifInterface ei = new ExifInterface(mFilePath);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            bitmap = rotateImage(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bitmap = rotateImage(bitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            bitmap = rotateImage(bitmap, 270);
                            break;
                    }

                    int bitmapSize = bitmap.getRowBytes() * bitmap.getHeight();
                    if (bitmapSize > 1) mDiskCache.put(mMd5Hex, bitmap);
                    else bitmap = null;
                } catch (NullPointerException e) {
                    bitmap = null;
                } catch (IOException e) {
                    bitmap = null;
                }
            }

            if (bitmap != null) mMemCache.putBitmap(mMd5Hex, bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (isCancelled()) {
                return;
            }

            if (bitmap == null) {
                mAdapter.remove(mImgPkg);
                return;
            }

            if (imageViewReference != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

        private static Bitmap rotateImage(Bitmap sourceBitmap, float rotation) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
        }
    }

    public static class AsyncDrawable extends BitmapDrawable {
        //class is used to tie an AsyncTask to an ImageView through
        //being set as the ImageView's Drawable
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String md5Hex, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String taskMd5Hex = bitmapWorkerTask.mMd5Hex;
            if (!taskMd5Hex.equals(md5Hex)) {
                //if task's image is different from new image, it means
                //this view was recycled for use with a different image,
                //so cancel the old task
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        //no task associated with ImageView, or existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        //gets the drawable of the ImageView.  If it is an AsyncDrawable,
        //then get the bitmap worker task that it holds
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}

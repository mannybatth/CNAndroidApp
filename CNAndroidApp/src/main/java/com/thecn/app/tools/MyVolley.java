package com.thecn.app.tools;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.thecn.app.models.PollItem;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Helper class that is used to provide references to initialized RequestQueue(s) and ImageLoader(s)
 *
 * @author Ognyan Bankov
 */
public class MyVolley {
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;

    private MyVolley() {
        // no instances
    }

    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQueue, BitmapLruCache.getInstance());
    }

    public static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    /**
     * Cancel all requests with the associated tag
     * @param tag
     */
    public static void cancelRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache} which effectively means
     * that no memory caching is used. This is useful for images that you know that will be show
     * only once.
     *
     * @return
     */
    public static ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

    /**
     * Similar to getImageListener in ImageLoader code, but checks to see if
     * the IndexedImageViewHolder's index is the same as when the request was made
     * If not, then don't set the image to the ImageView
     * @param index the current index of the IndexedImageViewHolder when the request is first made
     * @param holder the IndexedImageViewHolder (used to get index and ImageView)
     * @param defaultImageResId default image to show (if bitmap null)
     * @param errorImageResId image to show on network error
     * @return an ImageListener to be used by ImageLoader's get() method
     */
    public static IndexedImageListener getIndexedImageListener(final int index, final ImageView imageView,
                                                      final int defaultImageResId, final int errorImageResId) {

        return new IndexedImageListener(index, imageView, defaultImageResId, errorImageResId);

    }

    public static class IndexedImageListener implements ImageLoader.ImageListener {

        int errorImageResId, defaultImageResId, index;
        ImageView imageView;

        public IndexedImageListener(final int index, final ImageView imageView,
                                    final int defaultImageResId, final int errorImageResId) {
            this.index = index;
            this.imageView = imageView;
            this.defaultImageResId = defaultImageResId;
            this.errorImageResId = errorImageResId;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            int currIndex = getIntegerTag(imageView);

            if (errorImageResId != 0 && currIndex == index && imageView != null) {
                imageView.setImageResource(errorImageResId);
            }
        }
        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            int currIndex = getIntegerTag(imageView);

            if (currIndex == index) {
                if (imageView != null) {
                    if (response.getBitmap() != null) {
                        imageView.setImageBitmap(response.getBitmap());
                    } else if (defaultImageResId != 0) {
                        imageView.setImageResource(defaultImageResId);
                    }
                }
            }
        }
    }

    public static void loadImage(String url, ImageView dest, int position, int defaultResId) {
        dest.setTag(position);
        dest.setImageResource(defaultResId);

        try {
            mImageLoader.get(url,
                    getIndexedImageListener(position, dest,
                            defaultResId,
                            defaultResId));
        } catch (Exception e) {
            //data not there
        }
    }

    private static int getIntegerTag(View imageView) {
        Object object = imageView.getTag();

        if (object instanceof Integer) {
            return (Integer) object;
        }

        return -1;
    }

    /**
     * Trust every server - dont check for any certificate
     */
    public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setFollowRedirects(true);
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(final String hostname, final SSLSession session) {
                    return true;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

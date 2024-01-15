package com.thecn.app.stores;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.testflightapp.lib.TestFlight;
import com.thecn.app.AppSession;
import com.thecn.app.tools.MyVolley;
import com.thecn.app.tools.PausingHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BaseStore {

    private static final String TAG = BaseStore.class.getSimpleName();

    public static final String SITE_URL = "http://cndev.coursenetworking.com";
    //public static final String SITE_URL = "https://www.thecn.com";

    public static final String BASE_URL = SITE_URL + "/api";

    public static final boolean showLogging = true;

    public static class APIParams {
        String query;
        int method;
        ResponseCallback callback;

        JSONObject jsonObject;
        Map<String, String> headers;
        Object tag;

        public APIParams(String query, int method, ResponseCallback callback) {
            this.query = query;
            this.method = method;
            this.callback = callback;
        }
    }

    public static void api(String query,
                           int method,
                           ResponseCallback callback) {

        api(query, method, new HashMap<String, String>(), callback);
    }

    public static void api(String query,
                           int method,
                           Map<String, String> params,
                           ResponseCallback callback) {

        api(query, method, new JSONObject(params), callback);
    }

    public static void api(String query,
                           int method,
                           JSONObject jsonObject,
                           ResponseCallback callback) {

        api(query, method, jsonObject, getHeadersWithToken(), callback);
    }

    public static Map<String, String> getHeadersWithToken() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("token", AppSession.getInstance().getToken());
        return headers;
    }

    public static void api(String query,
                           int method,
                           Map<String, String> params,
                           final Map<String, String> headers,
                           final ResponseCallback callback) {

        api(query, method, new JSONObject(params), headers, callback);
    }

    public static void api(String query,
                           int method,
                           JSONObject jsonObject,
                           final Map<String, String> headers,
                           final ResponseCallback callback) {

        APIParams params = new APIParams(query, method, callback);
        params.jsonObject = jsonObject;
        params.headers = headers;

        api(params);
    }

    public static void api(final APIParams params) {

        RequestQueue queue = MyVolley.getRequestQueue();

        String url = BASE_URL + params.query;
        if (showLogging) Log.d(TAG, "API CALL => " + url);
        TestFlight.log("API CALL => " + url);

        if (params.jsonObject == null) {
            params.jsonObject = new JSONObject();
        }

        if (params.headers == null) {
            params.headers = getHeadersWithToken();
        }

        JsonObjectRequest request = new JsonObjectRequest(params.method, url, params.jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (showLogging) Log.d(TAG, response.toString());

                        doCallbacks(params.callback, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        doCallbacks(params.callback, error);
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                params.headers.put("User-Agent", getUserAgentString());
                return params.headers;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 1.0f));
        request.setShouldCache(Boolean.FALSE);
        if (params.tag != null) request.setTag(params.tag);

        queue.add(request);

        /*if (queue.getCache().get(url) != null) {

            // cache exists
            try {
                String cachedResponseStr = new String(queue.getCache().get(url).data);
                JSONObject response = new JSONObject(cachedResponseStr);
                Log.d(TAG, "CACHE: "+response.toString());
                callback.response(response);
                queue.add(request);
            } catch (JSONException e) {
                // something went wrong
            }

        } else {

            //no cache
            request.setShouldCache(Boolean.FALSE);
            queue.add(request);

        }*/

    }

    private static void doCallbacks(final ResponseCallback callback, final JSONObject response) {

        callback.onImmediateResponse();

        PausingHandler handler = callback.getHandler();

        boolean handlerNotNull = handler != null;
        boolean flagSet = false;
        int flags;
        int flagToCheck;

        final Runnable mainExecutionMethod;

        if (success(response)) {

            if (handlerNotNull) {
                flags = callback.getFlags();
                flagToCheck = ResponseCallback.HANDLE_SUCCESS;

                flagSet = (flags & flagToCheck) == flagToCheck;
            }

            callback.setExecutionType(ResponseCallback.ExecutionType.SUCCESS);
            mainExecutionMethod = new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(response);
                }
            };
        } else {

            if (handlerNotNull) {
                flags = callback.getFlags();
                flagToCheck = ResponseCallback.HANDLE_FAILURE;

                flagSet = (flags & flagToCheck) == flagToCheck;
            }

            callback.setExecutionType(ResponseCallback.ExecutionType.FAILURE);
            mainExecutionMethod = new Runnable() {
                @Override
                public void run() {
                    callback.onFailure(response);
                }
            };
        }

        Runnable execution = new Runnable() {
            @Override
            public void run() {
                callback.onPreExecute();
                mainExecutionMethod.run();
                callback.onPostExecute();
            }
        };

        if (flagSet) {
            handler.postWhenResumed(execution);
        } else {
            execution.run();
        }

        if (handlerNotNull) {
            handler.postWhenResumed(new Runnable() {
                @Override
                public void run() {
                    callback.executeWithHandler(response);
                }
            });
        }
    }

    private static void doCallbacks(final ResponseCallback callback, final VolleyError error) {

        callback.onImmediateResponse();
        callback.setExecutionType(ResponseCallback.ExecutionType.ERROR);

        PausingHandler handler = callback.getHandler();

        Runnable execution = new Runnable() {
            @Override
            public void run() {
                callback.onPostExecute();
                callback.onError(error);
                callback.onPostExecute();
            }
        };

        boolean handlerNotNull = handler != null;
        int flags = callback.getFlags();
        int flagToCheck = ResponseCallback.HANDLE_ERROR;
        boolean flagSet = handlerNotNull && (flags & flagToCheck) == flagToCheck;

        if (flagSet) {
            handler.postWhenResumed(execution);
        } else {
            execution.run();
        }

        if (handlerNotNull) {
            handler.postWhenResumed(new Runnable() {
                @Override
                public void run() {
                    callback.executeWithHandler(error);
                }
            });
        }
    }

    public static boolean success(JSONObject response) {
        try {
            return response.getBoolean("result");
        } catch (JSONException e) {
            return false;
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private static String getUserAgentString() {
        Context context = AppSession.getInstance().getApplicationContext();

        String versionName = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //can't get version name
        } catch (NullPointerException e) {
            //can't get version name
        }

        String string = "CNAndroidApp/";

        string += versionName + " ";
        string += "(Android; " + Build.MANUFACTURER + "; " + Build.MODEL + "; ";
        string += getVersionCode() + " " + Build.VERSION.RELEASE + "; ";
        string += getDensity(context) + ")";

        return string;
    }

    private static String getDensity(Context context) {
        String string;

        float density = context.getResources().getDisplayMetrics().density;

        if (density < 1f) string = "ldpi";
        else if (density < 1.5f) string = "mdpi";
        else if (density < 2.0f) string = "hdpi";
        else if (density < 3.0f) string = "xhdpi";
        else if (density < 4.0f) string = "xxhdpi";
        else string = "xxxhdpi";

        return string;
    }

    private static String getVersionCode() {

        switch (Build.VERSION.SDK_INT) {
            case 1:
                return "Base";
            case 2:
                return "Petit_Four";
            case 3:
                return "Cupcake";
            case 4:
                return "Donut";
            case 5:
                return "Eclair";
            case 6:
                return "Eclair";
            case 7:
                return "Eclair";
            case 8:
                return "Froyo";
            case 9:
                return "Gingerbread";
            case 10:
                return "Gingerbread";
            case 11:
                return "Honeycomb";
            case 12:
                return "Honeycomb";
            case 13:
                return "Honeycomb";
            case 14:
                return "Ice_Cream_Sandwich";
            case 15:
                return "Ice_Cream_Sandwich";
            case 16:
                return "Jelly_Bean";
            case 17:
                return "Jelly_Bean";
            case 18:
                return "Jelly_Bean";
            case 19:
                return "KitKat";
            default:
                return "";
        }
    }
}

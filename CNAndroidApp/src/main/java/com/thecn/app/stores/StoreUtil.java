package com.thecn.app.stores;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.testflightapp.lib.TestFlight;
import com.thecn.app.AppSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by philjay on 7/3/14.
 */
public class StoreUtil {

    public static final String TAG = StoreUtil.class.getSimpleName();

    public static boolean getResult(JSONObject response) {
        try {
            return response.getBoolean("result");
        } catch (JSONException e) {
            return false;
        }
    }

    public static ArrayList<String> getResponseErrors(JSONObject response) {
        try {
            JSONArray jsonArray = response.getJSONArray("errors");
            ArrayList<String> retVal = new ArrayList<String>();

            for (int i = 0; i < jsonArray.length(); i ++) {
                try {
                    String error = jsonArray.getString(i);
                    if (error != null) {
                        retVal.add(error);
                    }
                } catch (JSONException e) {
                    //well that's weird
                }
            }

            return retVal.size() > 0 ? retVal : null;
        } catch (JSONException e) {
            return null;
        }
    }

    public static void showFirstResponseError(JSONObject response) {
        ArrayList<String> errors = StoreUtil.getResponseErrors(response);
        if (errors != null && errors.size() > 0) {
            String error = errors.get(0);
            if (error != null) {
                AppSession.showToast(error);
            }
        }
    }

    public static int getNextOffset(JSONObject response) {
        try {
            return response.getJSONObject("extra_data").getInt("next_offset");
        } catch (JSONException e) {
            return -1;
        }
    }

    public static void showExceptionMessage(Exception error) {

        try {

            if (error == null) return;

            if (error instanceof VolleyError) {

                VolleyError volleyError = (VolleyError) error;

                Log.d(TAG, "Error Response Reason: " + volleyError.toString());
                TestFlight.log("Error Response Reason: " + volleyError.toString());

                if (volleyError instanceof NoConnectionError) {
                    Log.d(TAG, "Volley Error: NoConnectionError");
                    AppSession.showToast("No internet connection");
                } else if (volleyError instanceof ServerError) {
                    Log.d(TAG, "Volley Error: ServerError");
                    Log.d(TAG, "Volley Error: ServerError Code: " + volleyError.networkResponse.statusCode);
                    TestFlight.log("Volley Error: ServerError Code: " + volleyError.networkResponse.statusCode);
                    AppSession.showToast("A problem with the server occurred");
                } else if (volleyError instanceof AuthFailureError) {
                    Log.d(TAG, "Volley Error: AuthFailureError");
                    AppSession.showToast("Authentication Failure");
                } else if (volleyError instanceof ParseError) {
                    Log.d(TAG, "Volley Error: ParseError");
                    AppSession.showToast("A parsing error occurred");
                } else if (volleyError instanceof NetworkError) {
                    Log.d(TAG, "Volley Error: NetworkError");
                    AppSession.showToast("A problem with the network occurred");
                } else if (volleyError instanceof TimeoutError) {
                    Log.d(TAG, "Volley Error: TimeoutError");
                    AppSession.showToast("The connection timed out");
                } else {
                    if (volleyError.networkResponse != null) {
                        Log.d(TAG, "Volley Error: ClientError Code: " + volleyError.networkResponse.statusCode);
                        TestFlight.log("Volley Error: ClientError Code: " + volleyError.networkResponse.statusCode);
                        AppSession.showToast("No network response");
                    }
                }
            }


        } catch (NullPointerException e) {
            // no error message...
        }
    }

    public static void logNullAtIndex(int index) {
        Log.e(TAG, "JSONObject null at index: " + index);
    }

    public static void logNullAtIndex(String tag, int index) {
        Log.e(tag, "JSONObject null at index: " + index);
    }
}

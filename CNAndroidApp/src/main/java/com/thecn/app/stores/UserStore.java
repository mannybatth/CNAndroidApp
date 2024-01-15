package com.thecn.app.stores;

import android.app.DownloadManager;
import android.util.Log;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.User.User;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class UserStore extends BaseStore {

    private static final String TAG = UserStore.class.getSimpleName();

    public static User getData(JSONObject response) {
        try {
            JSONObject jsonObject = response.getJSONObject("data");
            return fromJSON(jsonObject);
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static ArrayList<User> getListData(JSONObject response) {
        try {
            JSONArray jsonArray = response.getJSONArray("data");
            return fromJSON(jsonArray);
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static ArrayList<User> getRosterData(JSONObject response) {
        try {
            JSONArray jsonArray = response.getJSONArray("data");
            ArrayList<User> users = new ArrayList<User>();
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    User user = fromJSON(jsonArray.getJSONObject(i).getJSONObject("model"));
                    if (user != null) {
                        users.add(user);
                    } else {
                        StoreUtil.logNullAtIndex(TAG, i);
                    }
                } catch (Exception e) {
                    Log.d(TAG + ": ERROR", e.getMessage());
                }
            }

            return users.size() > 0 ? users : null;
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static User fromJSON(JSONObject jsonObject) {
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(jsonObject.toString(), User.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static ArrayList<User> fromJSON(JSONArray jsonArray) {
        ArrayList<User> users = new ArrayList<User>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                User user = fromJSON(jsonArray.getJSONObject(i));
                if (user != null) {
                    users.add(user);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (JSONException e) {
                //do nothing
            } catch (NullPointerException e) {
                //do nothing
            }
        }

        return users.size() > 0 ? users : null;
    }

    public static void getMe(final ResponseCallback callback) {

        api("/me?&with_user_count=1&with_user_country=1", Request.Method.GET, callback);
    }

    public static void getUserById(String userId, final ResponseCallback callback) {
        getUserById(userId, "?with_user_profile=1&with_user_score=1&" +
                "with_user_count=1&with_user_country=1&with_user_relations=1", callback);
    }

    public static void getUserById(String userId, String params, final ResponseCallback callback) {
        api("/user/" + userId + params, Request.Method.GET, callback);
    }

    public static void getUserByCNNumber(String cnNumber, final ResponseCallback callback) {

        getUserByCNNumber(cnNumber,
                "?with_user_profile=1&with_user_score=1&with_user_count=1&with_user_country=1&with_user_relations=1",
                callback);
    }

    public static void getUserByCNNumber(String cnNumber, String params, final ResponseCallback callback) {
        api("/cn_number/" + cnNumber + params, Request.Method.GET, callback);
    }

    public static void getAllUserCourses(final ResponseCallback callback) {

        api("/user_course?limit=999", Request.Method.GET, callback);
    }

    public static void getAllUserConexuses(final ResponseCallback callback) {

        api("/user_conexus?limit=999", Request.Method.GET, callback);
    }

    public static void followUser(String userId, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("follow_user_id", userId);

        try {
            Gson gson = GlobalGson.getGson();
            JSONObject json = new JSONObject(gson.toJson(params));
            api("/user_following/", Request.Method.POST, json, callback);
        } catch (JSONException e) {
            // something went wrong
        }
    }

    public static void stopFollowingUser(String userId, ResponseCallback callback) {

        api("/user_following/" + userId, Request.Method.DELETE, callback);
    }

    public static void getPollRespondents(String contentId, String itemId, int limit, int offset, ResponseCallback callback) {
        String query = "/survey_submission/?content_id=" + contentId + "&item_id=" + itemId + "&limit=" + limit + "&offset=" + offset
                + "&with_user_country=1&with_user_score=1&with_survey_submission_user=1";

        api(query, Request.Method.GET, callback);
    }
}

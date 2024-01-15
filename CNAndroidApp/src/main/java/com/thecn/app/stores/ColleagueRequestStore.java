package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.ColleagueRequest;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by philjay on 5/1/14.
 */
public class ColleagueRequestStore extends BaseStore {

    public static final String TAG = ColleagueRequestStore.class.getSimpleName();

    public static ArrayList<ColleagueRequest> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static ColleagueRequest fromJSON(JSONObject jsonObject) {
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(jsonObject.toString(), ColleagueRequest.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static ArrayList<ColleagueRequest> fromJSON(JSONArray jsonArray) {
        ArrayList<ColleagueRequest> requests = new ArrayList<ColleagueRequest>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                ColleagueRequest request = fromJSON(jsonArray.getJSONObject(i));
                if (request != null) {
                    requests.add(request);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (JSONException e) {
                //do nothing
            } catch (NullPointerException e) {
                //do nothing
            }
        }

        return requests.size() > 0 ? requests : null;
    }

    public static void getRequests(int limit, int offset, final ResponseCallback callback) {
        String query = "/user_colleague_request/?limit=" + limit + "&offset=" + offset;

        api(query, Request.Method.GET, callback);
    }

    public static void replyToRequest(final boolean accepted, String userID, final ResponseCallback callback) {
        int status = accepted ? 1 : 0;
        String query = "/user_colleague/" + userID + "?status=" + status;

        api(query, Request.Method.PUT, callback);
    }

    public static void sendRequest(String userId, final ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("colleague_user_id", userId);

        try {
            Gson gson = GlobalGson.getGson();
            JSONObject json = new JSONObject(gson.toJson(params));
            api("/user_colleague/", Request.Method.POST, json, callback);
        } catch (JSONException e) {
            // something went wrong
        }

    }

    public static void removeColleague(String colleagueId, final ResponseCallback callback) {
        api("/user_colleague/" + colleagueId, Request.Method.DELETE, callback);
    }

    public static void cancelRequest(String userId, final ResponseCallback callback) {
        api("/user_colleague/" + userId, Request.Method.DELETE, callback);
    }
}

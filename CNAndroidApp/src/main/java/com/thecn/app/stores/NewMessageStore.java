package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.UserNewMessage;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by philjay on 5/5/14.
 */
public class NewMessageStore extends BaseStore {

    public static UserNewMessage getData(JSONObject response) {
        try {
            return fromJSON(response.getJSONObject("data").getJSONArray("user_new_message"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static UserNewMessage fromJSON(JSONArray jsonArray) {
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(jsonArray.toString(), UserNewMessage.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static void getNewMessages(final ResponseCallback callback) {

        api("/v2/user_new_message", Request.Method.GET, callback);
    }
}

package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.Notification;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by philjay on 4/16/14.
 */
public class NotificationStore extends BaseStore {

    public static final String TAG = NotificationStore.class.getSimpleName();

    public static ArrayList<Notification> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Notification fromJSON(JSONObject jsonObject) {
        Gson gson = GlobalGson.getGson();

        try {
            return gson.fromJson(jsonObject.toString(), Notification.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static ArrayList<Notification> fromJSON(JSONArray jsonArray) {
        ArrayList<Notification> notifications = new ArrayList<Notification>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Notification notification = fromJSON(jsonArray.getJSONObject(i));
                if (notification != null) {
                    notifications.add(notification);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (JSONException e) {
                //do nothing
            } catch (NullPointerException e) {
                //do nothing
            }
        }

        return notifications.size() > 0 ? notifications : null;
    }

    public static void getNotifications(int limit, int offset, final ResponseCallback callback) {
        String query = "/user_notification/?with_user_notification_extra_data=1" +
                "&limit=" + limit + "&offset=" + offset;

        api(query, Request.Method.GET, callback);
    }

    public static void markNotificationRead(String id) {
        String query = "/user_notification/" + id + "?mark=read";

        api(query, Request.Method.PUT, new ResponseCallback() {});
    }
}

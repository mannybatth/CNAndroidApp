package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.Email;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by philjay on 4/24/14.
 */
public class EmailStore extends BaseStore {

    public static final String TAG = EmailStore.class.getSimpleName();

    public static Email getData(JSONObject response) {
        try {
            return fromJSON(response.getJSONObject("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static ArrayList<Email> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Email fromJSON(JSONObject jsonObject) {
        Gson gson = GlobalGson.getGson();

        try {
            String jsonString = jsonObject.toString();
            return gson.fromJson(jsonString, Email.class);
        } catch (JsonSyntaxException e) {
            //something went wrong
            return null;
        }
    }

    public static ArrayList<Email> fromJSON(JSONArray jsonArray) {
        ArrayList<Email> emails = new ArrayList<Email>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Email email = fromJSON(jsonArray.getJSONObject(i));
                if (email != null) {
                    emails.add(email);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (JSONException e) {
                //do nothing
            } catch (NullPointerException e) {
                //do nothing
            }
        }

        return emails.size() > 0 ? emails : null;
    }

    public static final Object SEND_EMAIL_TAG = new Object();

    public static void sendEmail(JSONObject email, final ResponseCallback callback) {
        sendEmail(email, "", callback);
    }

    public static void sendEmail(JSONObject email, String queryParams, final ResponseCallback callback) {
        String query = "/email/" + queryParams;

        BaseStore.APIParams params = new APIParams(query, Request.Method.POST, callback);
        params.jsonObject = email;
        params.tag = SEND_EMAIL_TAG;

        api(params);
    }

    public static void getEmails(int limit, int offset, final ResponseCallback callback) {
        String query = "/email/?with_email_extra_data=1&with_email_sender=1" +
                "&email_list_order=most_new&limit=" + limit + "&offset=" + offset;

        api(query, Request.Method.GET, callback);
    }

    public static void getEmailById(String id, final ResponseCallback callback) {
        String query = "/email/" + id + "?with_email_sender=1&with_email_sub_emails=1" +
                "&with_email_extra_data=1&with_email_attachments=1&with_email_videos=1" +
                "&with_email_pictures=1";

        api(query, Request.Method.GET, callback);
    }

    public static void markEmailRead(String id) {
        String query = "/email/" + id + "?status=read";

        api(query, Request.Method.PUT, new ResponseCallback() {});
    }

    public static void delete(String id, final ResponseCallback callback) {

        api("/email/" + id, Request.Method.DELETE, callback);
    }
}

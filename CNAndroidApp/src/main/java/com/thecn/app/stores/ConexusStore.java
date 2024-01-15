package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ConexusStore extends BaseStore{

    private static final String TAG = ConexusStore.class.getSimpleName();

    public static Conexus getData(JSONObject response) {
        try {
            return fromJSON(response.getJSONObject("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static ArrayList<Conexus> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Conexus fromJSON(JSONObject json) {

        Gson gson = GlobalGson.getGson();
        try {
            Conexus conexus = gson.fromJson(json.toString(), Conexus.class);
            return conexus;
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static ArrayList<Conexus> fromJSON(JSONArray jsonArray) {
        ArrayList<Conexus> conexuses = new ArrayList<Conexus>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Conexus conexus = fromJSON(jsonArray.getJSONObject(i));
                if (conexus != null) {
                    conexuses.add(conexus);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (JSONException e) {
                //do nothing
            } catch (NullPointerException e) {
                //do nothing
            }
        }

        return conexuses.size() > 0 ? conexuses : null;
    }

    public static void getConexusById(String conexusID, final ResponseCallback callback) {
        String query = "/conexus/" + conexusID + "?with_conexus_country=1"
                + "&with_conexus_moderators=1&with_conexus_user_score=1&with_conexus_content_count=1"
                + "&with_conexus_user_content_count=1&with_conexus_count=1";

        api(query, Request.Method.GET, callback);
    }

    public static void getConexusByNumber(String conexusNumber, final ResponseCallback callback) {
        String query = "/conexus_number/" + conexusNumber + "?with_conexus_country=1"
                + "&with_conexus_moderators=1&with_conexus_user_score=1&with_conexus_content_count=1"
                + "&with_conexus_user_content_count=1&with_conexus_count=1";

        api(query, Request.Method.GET, callback);
    }

    public static void getConexusRoster(Conexus conexus, int limit, int offset, final ResponseCallback callback) {
        String query = "/conexus_user/?conexus_id=" + conexus.getId() + "&limit=" + limit + "&offset=" + offset
                + "&with_user_country=1&with_user_profile=1&with_user_score=1&with_conexus_user_model=1" +
                "&with_conexus_user_count=1&with_conexus_user_score=1";

        api(query, Request.Method.GET, callback);
    }

    public static void joinConexus(String conexusID, String userID, String inviteEmailID, final ResponseCallback callback) {
        String query = "/conexus_user/" + conexusID + "?user_id=" + userID + "&invite_email_id=" + inviteEmailID;

        api(query, Request.Method.PUT, callback);
    }
}

package com.thecn.app.stores;

import com.android.volley.Request;
import com.thecn.app.models.User.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by philjay on 6/10/14.
 */
public class SearchStore extends BaseStore {

    public static final String TAG = SearchStore.class.getSimpleName();

    public static ArrayList<User> getUserListData(JSONObject response) {

        ArrayList<User> users = new ArrayList<User>();

        try {
            JSONArray jArray = response.getJSONArray("data");

            for (int i = 0; i < jArray.length(); i++) {
                try {
                    JSONObject jObject = jArray.getJSONObject(i);
                    String type = jObject.getString("type");

                    if (type != null && type.equals("user")) {
                        JSONObject userJObject = jObject.getJSONObject("model");
                        User user = UserStore.fromJSON(userJObject);
                        if (user != null) users.add(user);
                        else StoreUtil.logNullAtIndex(TAG, i);
                    }
                } catch (JSONException e) {
                    //whoops
                } catch (NullPointerException e) {
                    //whoops
                }
            }

        } catch (JSONException e) {
            //whoops
        } catch (NullPointerException e) {
            //whoops
        }

        return users;
    }

    public static void userSearchByCNNumber(String keyword, int limit, final ResponseCallback callback) {
        String query = "/search_user_cn_number/?keyword=" + keyword + "&limit=" + limit;
        query += "&with_user_country=1";

        api(query, Request.Method.GET, callback);
    }

    public static void userSearchByKeyword(String keyword, int limit, final ResponseCallback callback) {
        String query = "/search_user/?keyword=" + keyword + "&limit=" + limit;
        query += "&with_user_country=1";

        api(query, Request.Method.GET, callback);
    }
}

package com.thecn.app.stores;


import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.Post;
import com.thecn.app.models.Reflection;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ReflectionStore extends BaseStore {

    public static final String TAG = ReflectionStore.class.getSimpleName();

    public static Reflection getData(JSONObject response) {
        try {
            return fromJSON(response.getJSONObject("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static ArrayList<Reflection> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Reflection fromJSON(JSONObject jsonObject) {
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(jsonObject.toString(), Reflection.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static ArrayList<Reflection> fromJSON(JSONArray jsonArray) {
        ArrayList<Reflection> reflections = new ArrayList<Reflection>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Reflection reflection = fromJSON(jsonArray.getJSONObject(i));
                if (reflection != null) {
                    reflections.add(reflection);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (JSONException e) {
                //do nothing
            } catch (NullPointerException e) {
                //do nothing
            }
        }

        return reflections.size() > 0 ? reflections : null;
    }

    public static void getPostReflections(String postId, int limit, int offset, final ResponseCallback callback) {
        String apiMethod = "content_comment";
        String listOrder = "content_comment_list_order=most_new";

        String query = "/" + apiMethod + "/?with_content_count=1&with_content_user=1&with_content_original_content=1&" +
                "with_content_attachments=1&with_content_pictures=1&with_content_links=1&with_content_videos=1&" +
                "with_content_comments=1&with_content_comment_user=1&with_user_country=1&user_content_list_order=most_new&" +
                "with_content_comment_sub_comments=1&without_content_comment_autolink=0&content_id=" + postId + "&" + listOrder + "&limit=" + limit + "&offset=" + offset;

        api(query, Request.Method.GET, callback);
    }

    public static void makeReflection(final Post post, final String text, final ResponseCallback callback) {
        String apiMethod = "content_comment";

        String query = "/" + apiMethod + "?return_detail=1&with_content_comment_user=1"
                + "&with_user_profile=1";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("content_id", post.getId());
        params.put("text", text);

        api(query, Request.Method.POST, params, callback);
    }

    public static void likeReflection(final Reflection theReflection, final ResponseCallback callback) {
        String apiMethod = "content_comment_good";

        String query = "/" + apiMethod;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("content_comment_id", theReflection.getId());

        api(query, Request.Method.POST, params, callback);
    }

    public static void unlikeReflection(final Reflection theReflection, final ResponseCallback callback) {
        String apiMethod = "content_comment_good";

        String query = "/" + apiMethod + "/" + theReflection.getId();

        api(query, Request.Method.DELETE, callback);
    }
}

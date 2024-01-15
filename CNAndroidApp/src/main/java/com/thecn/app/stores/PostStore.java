package com.thecn.app.stores;

import android.util.Log;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.Post;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PostStore extends BaseStore {

    public static final String TAG = PostStore.class.getSimpleName();

    public static Post getData(JSONObject response) {
        try {
            return fromJSON(response.getJSONObject("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static ArrayList<Post> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Post fromJSON(JSONObject json) {

        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(json.toString(), Post.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static ArrayList<Post> fromJSON(JSONArray jsonArray) {

        ArrayList<Post> posts = new ArrayList<Post>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Post post = fromJSON(jsonArray.getJSONObject(i));
                if (post != null) {
                    String postType = post.getPostType();
                    if (postType != null && postType.equals("post")
                            || postType.equals("survey")
                            || postType.equals("event")
                            || postType.equals("quiz")
                            || postType.equals("classcast")
                            || postType.equals("sharelink")) { // remove this later
                        posts.add(post);
                    }
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (JSONException e) {
                //do nothing
            } catch (NullPointerException e) {
                //do nothing
            }
        }

        return posts.size() > 0 ? posts : null;
    }

    public static void makePost(String text,
                                String[] courseRelations,
                                String[] conexusRelations,
                                String[] groupIDs,
                                final ResponseCallback callback) {

        makePost(text, courseRelations, conexusRelations, groupIDs, new String[]{}, new String[]{}, callback);
    }

    public static void makePost(String text,
                                String[] courseRelations,
                                String[] conexusRelations,
                                String[] groupIDs,
                                String[] imageIDs,
                                String[] youtubeLinks,
                                final ResponseCallback callback) {

        String apiMethod = "post";

        String query = "/" + apiMethod + "/?return_detail=1&with_content_count=1&with_content_user=1"
                + "&with_content_original_content=1&with_content_attachments=1&with_content_pictures=1"
                + "&with_content_links=1&with_content_videos=1&with_content_courses=1"
                + "&with_content_conexuses=1&with_user_country=1&with_user_profile=1";

        HashMap<String, String[]> relations = new HashMap<String, String[]>();
        relations.put("course_ids", courseRelations);
        relations.put("conexus_ids", conexusRelations);

        ArrayList<String> videoObject = new ArrayList<String>(Arrays.asList(youtubeLinks));

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("text", text);
        params.put("relations", relations);
        params.put("auth_assignment_group_ids", groupIDs);
        params.put("pictures", imageIDs);
        params.put("videos", videoObject);

        try {
            Gson gson = GlobalGson.getGson();
            JSONObject json = new JSONObject(gson.toJson(params));
            api(query, Request.Method.POST, json, callback);
        } catch (JSONException e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    public static void likePost(final Post post, final ResponseCallback callback) {
        String apiMethod = "content_good";

        String query = "/" + apiMethod;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("content_id", post.getId());

        api(query, Request.Method.POST, params, callback);
    }

    public static void unlikePost(final Post post, final ResponseCallback callback) {
        String apiMethod = "content_good";

        String query = "/" + apiMethod + "/" + post.getId();

        api(query, Request.Method.DELETE, callback);
    }

    public static void getHomePostsWithFilter(String filter, int limit, int offset, final ResponseCallback callback) {
        String apiMethod = "content";
        String listOrder = "content_list_order=most_new";

        String query = "/" + apiMethod + "/?with_content_count=1&with_content_user=1&with_content_original_content=1&" +
                "with_content_attachments=1&with_content_pictures=1&with_content_links=1&with_content_videos=1&" +
                "with_user_country=1&" + listOrder + "&limit=" + limit + "&offset=" + offset +
                "&with_content_courses=1&with_content_conexuses=1";

        api(query, Request.Method.GET, callback);
    }

    public static void getPostById(String postId, final ResponseCallback callback) {

        String query = "/content/" + postId + "?with_content_count=1&with_content_user=1" +
                "&with_content_original_content=1&with_content_attachments=1&with_content_pictures=1" +
                "&with_content_links=1&with_content_videos=1&with_user_country=1" +
                "&with_content_courses=1&with_content_conexuses=1";

        api(query, Request.Method.GET, callback);
    }

    public static void getPostsFromUser(String userId, int limit, int offset, final ResponseCallback callback) {

        String query = "/user_content/?with_content_count=1&with_content_user=1&with_content_original_content=1&" +
                "with_content_attachments=1&with_content_pictures=1&with_content_links=1&with_content_videos=1&" +
                "with_user_country=1&user_content_list_order=most_new&user_id=" + userId + "&limit=" + limit + "&offset=" + offset +
                "&with_content_courses=1&with_content_conexuses=1";

        api(query, Request.Method.GET, callback);

    }

    public static void getPostsFromCourse(String courseId, int limit, int offset, final ResponseCallback callback) {

        String query = "/course_content/?with_content_count=1&with_content_user=1&with_content_original_content=1&" +
                "with_content_attachments=1&with_content_pictures=1&with_content_links=1&with_content_videos=1&" +
                "with_content_comments=0&with_content_comment_user=0&with_content_courses=1&with_content_conexuses=1&" +
                "with_user_country=1&with_user_profile=0&course_content_list_order=most_new&with_content_comment_sub_comments=0&" +
                "course_id=" + courseId + "&limit=" + limit + "&offset=" + offset;

        api(query, Request.Method.GET, callback);

    }

    public static void getPostsFromConexus(String conexusId, int limit, int offset, final ResponseCallback callback) {

        String query = "/conexus_content/?with_content_count=1&with_content_user=1&with_content_original_content=1&" +
                "with_content_attachments=1&with_content_pictures=1&with_content_links=1&with_content_videos=1&" +
                "with_content_comments=0&with_content_comment_user=0&with_content_courses=1&with_content_conexuses=1&" +
                "with_user_country=1&with_user_profile=0&course_content_list_order=most_new&with_content_comment_sub_comments=0&" +
                "conexus_id=" + conexusId + "&limit=" + limit + "&offset=" + offset;

        api(query, Request.Method.GET, callback);

    }

    public static void getPostLikes(String postId, int limit, int offset, ResponseCallback callback) {

        String query = "/content_good/?with_user_profile=0&with_user_relations=0&with_user_country=1&with_user_count=0&with_user_score=1&" +
                "content_id=" + postId + "&limit=" + limit + "&offset=" + offset;

        api(query, Request.Method.GET, callback);
    }

    public static void deletePost(String postId, final ResponseCallback callback) {

        String query = "/post/" + postId;

        api(query, Request.Method.DELETE, callback);
    }
}

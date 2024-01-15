package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.Course.Task;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CourseStore extends BaseStore{

    public static final String TAG = CourseStore.class.getSimpleName();

    public static Course getData(JSONObject response) {
        try {
            return fromJSON(response.getJSONObject("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static ArrayList<Course> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Course fromJSON(JSONObject json) {

        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(json.toString(), Course.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static ArrayList<Course> fromJSON(JSONArray jsonArray) {
        ArrayList<Course> courses = new ArrayList<Course>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Course course = fromJSON(jsonArray.getJSONObject(i));
                if (course != null) {
                    courses.add(course);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (JSONException e) {
                //do nothing
            } catch (NullPointerException e) {
                //do nothing
            }
        }

        return courses.size() > 0 ? courses : null;
    }

    public static Task getTaskData(JSONObject response) {
        try {
            boolean success = response.getBoolean("result");

            if (success) {
                return taskFromJSON(response.getJSONObject("data"));
            }
        } catch (JSONException e) {
            //proceed...
        } catch (NullPointerException e) {
            //proceed...
        }

        return null;
    }

    public static ArrayList<Task> getTaskListData(JSONObject response) {
        try {
            return tasksFromJSON(response.getJSONArray("data"));
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Task taskFromJSON(JSONObject json) {
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(json.toString(), Task.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static ArrayList<Task> tasksFromJSON(JSONArray jsonArray) {
        ArrayList<Task> tasks = new ArrayList<Task>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Task task = taskFromJSON(jsonArray.getJSONObject(i));
                if (task != null) {
                    tasks.add(task);
                } else {
                    StoreUtil.logNullAtIndex(TAG + " Task", i);
                }
            } catch (JSONException e) {
                //do nothing
            } catch (NullPointerException e) {
                //do nothing
            }
        }

        return tasks.size() > 0 ? tasks : null;
    }

    public static void getCourseById(String courseId, final ResponseCallback callback) {
        String query = "/course/" + courseId + "?with_course_school=1&with_course_country=1"
                + "&with_course_tasks=1&with_course_country=1&with_course_instructor_users=1"
                + "&with_course_user_score=1&with_course_score=1&with_course_score_expected_today=1"
                + "&with_course_score_setting=1&with_course_most_course_score_users=1"
                + "&with_course_user_model=1&with_course_count=1&with_course_least_course_score_users=1";

        api(query, Request.Method.GET, callback);
    }

    public static void getTaskDetails(String id, final ResponseCallback callback) {
        String query = "/task_details/" + id + "?with_content_current_user_is_observer=1" +
                "&with_content_comment_attachments=1&with_content_comment_pictures=1";

        api(query, Request.Method.GET, callback);
    }

    public static void getCourseTasks(Course course, final ResponseCallback callback) {
        api("/task/?course_id=" + course.getId(), Request.Method.GET, callback);
    }

    public static void getCourseRoster(Course course, int limit, int offset, final ResponseCallback callback) {
        String query = "/course_user/?course_id=" + course.getId() + "&limit=" + limit + "&offset=" + offset
                + "&with_user_country=1&with_user_profile=1&with_user_score=1&with_course_user_model=1" +
                "&with_course_user_count=1&with_course_user_score=1";

        api(query, Request.Method.GET, callback);
    }

    public static void joinCourse(String courseID, String userID, String inviteEmailID, final ResponseCallback callback) {
        String query = "/course_user/" + courseID + "?user_id=" + userID + "&invite_email_id=" + inviteEmailID;

        api(query, Request.Method.PUT, callback);
    }
}

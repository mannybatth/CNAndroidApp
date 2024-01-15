package com.thecn.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.thecn.app.models.Avatar;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.User.User;
import com.thecn.app.models.UserNewMessage;
import com.thecn.app.tools.GlobalGson;

import java.util.ArrayList;

public class AppSession {

    private static AppSession sharedSession = new AppSession();

    public static AppSession getInstance() {
        return sharedSession;
    }

    private String mToken;
    private final Object tokenLock = new Object();

    private User mUser;
    private final Object userLock = new Object();

    private UserNewMessage mUserNewMessage;
    private final Object mUserMessageLock = new Object();

    private SharedPreferences sharedPrefs;
    private CNApp mApp;

    private static String PREF_USER = "mUser";
    private static String PREF_TOKEN = "mToken";

    private AppSession() {
    }

    public void Initialize(CNApp app) {
        mApp = app;
        mAppToast = Toast.makeText(AppSession.getInstance().getApplicationContext(), "", Toast.LENGTH_LONG);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mApp);
        mUserNewMessage = new UserNewMessage();
    }

    public CNApp getApplicationContext() {
        return mApp;
    }

    public void clearSession() {
        synchronized (tokenLock) {
            setToken(null);
        }
        synchronized (userLock) {
            setUser(null);
        }
    }

    public User getUser() {
        synchronized (userLock) {
            if (mUser == null) {
                getUserFromPref();
            }

            if (mUser == null) {
                return null;
            }

            User copy = new User(mUser.getId());

            copy.setDisplayName(mUser.getDisplayName());
            copy.setCNNumber(mUser.getCNNumber());
            copy.setFirstName(mUser.getFirstName());
            copy.setLastName(mUser.getLastName());
            copy.setCountry(mUser.getCountry());

            Avatar avatar = mUser.getAvatar();
            avatar = new Avatar(avatar.getId(), avatar.getView_url());
            copy.setAvatar(avatar);

            return copy;
        }
    }

    private void getUserFromPref() {
        String json = sharedPrefs.getString(PREF_USER, null);
        Gson gson = GlobalGson.getGson();

        mUser = gson.fromJson(json, User.class);
    }

    public void setUser(User user) {
        synchronized (userLock) {

            mUser = user;

            Gson gson = GlobalGson.getGson();
            String userJson = gson.toJson(user);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(PREF_USER, userJson);
            editor.apply();
        }
    }

    public String getToken() {
        synchronized (tokenLock) {
            if (mToken != null) return mToken;
            return sharedPrefs.getString(PREF_TOKEN, null);
        }
    }

    public void setToken(String token) {
        synchronized (tokenLock) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(PREF_TOKEN, token);
            editor.apply();
            mToken = token;
        }
    }

    public ArrayList<Course> getSimpleUserCourses() {
        synchronized (userLock) {

            if (mUser == null) {
                getUserFromPref();
            }

            if (mUser != null && mUser.getCourses() != null) {

                ArrayList<Course> courses = new ArrayList<Course>();

                for (Course original : mUser.getCourses()) {
                    Course copy = new Course(original.getId());
                    copy.setName(original.getName());
                    copy.setLogoURL(original.getLogoURL());

                    courses.add(copy);
                }

                return courses;
            }

            return null;
        }
    }

    public ArrayList<Conexus> getSimpleUserConexuses() {
        synchronized (userLock) {

            if (mUser == null) {
                getUserFromPref();
            }

            if (mUser != null && mUser.getConexuses() != null) {

                ArrayList<Conexus> conexuses = new ArrayList<Conexus>();

                for (Conexus original : mUser.getConexuses()) {
                    Conexus copy = new Conexus(original.getId());
                    copy.setName(original.getName());
                    copy.setLogoURL(original.getLogoURL());

                    conexuses.add(copy);
                }

                return conexuses;
            }

            return null;
        }
    }

    public void setUserCourses(ArrayList<Course> courses) {
        synchronized (userLock) {
            mUser.setCourses(courses);
        }

        broadcastUpdatedCourses();
    }

    public void setUserConexuses(ArrayList<Conexus> conexuses) {
        synchronized (userLock) {
            mUser.setConexuses(conexuses);
        }

        broadcastUpdatedConexuses();
    }

    public UserNewMessage getUserNewMessage() {
        synchronized (mUserMessageLock) {
            return UserNewMessage.getCopy(mUserNewMessage);
        }
    }

    public void setUserNewMessage(UserNewMessage userNewMessage) {
        synchronized (mUserMessageLock) {
            mUserNewMessage = UserNewMessage.getCopy(userNewMessage);
        }

        broadcastNewNotifications();
    }

    private void broadcastNewNotifications() {
        Intent intent = new Intent("onNotificationChange");
        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    private void broadcastUpdatedCourses() {
        Intent intent = new Intent("onCourseChange");
        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    private void broadcastUpdatedConexuses() {
        Intent intent = new Intent("onConexusChange");
        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    /**
     * Convenience methods for showing toast
     */

    private Toast mAppToast;

    public Toast getAppToast() {
        return mAppToast;
    }

    private static Toast getToast() {
        if (sharedSession != null) {
            return sharedSession.getAppToast();
        }

        return null;
    }

    public static void showDataLoadError(String dataNotLoaded) {
        showToast("Could not load " + dataNotLoaded + " data.");
    }

    public static void showToast(String message) {
        Toast toast = getToast();
        if (toast != null) {
            toast.setText(message);
            toast.show();
        }
    }

    public static void dismissToast() {
        Toast toast = getToast();
        if (toast != null) {
            toast.cancel();
        }
    }
}

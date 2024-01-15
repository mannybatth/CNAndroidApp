package com.thecn.app.stores;

import com.android.volley.Request;

import java.util.HashMap;
import java.util.Map;

public class AuthStore extends BaseStore {

    public static void login(String username, String password, final ResponseCallback callback) {

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("username", username);
        headers.put("password", password);

        api("/auth?action=login", Request.Method.GET, new HashMap<String, String>(), headers, callback);

    }

    public static void logout(final ResponseCallback callback) {

        api("/auth?action=logout", Request.Method.GET, callback);

    }
}

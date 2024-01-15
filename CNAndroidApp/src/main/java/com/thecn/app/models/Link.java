package com.thecn.app.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Link implements Serializable {

    @SerializedName("display_url")
    private String displayUrl;

    @SerializedName("view_url")
    private String viewUrl;

    public String getDisplayUrl() {
        return displayUrl;
    }

    public void setDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }
}

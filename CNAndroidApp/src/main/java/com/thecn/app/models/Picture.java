package com.thecn.app.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Picture implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String pictureName;

    @SerializedName("view_url")
    private String pictureURL;

    @SerializedName("extension")
    private String pictureExt;

    @SerializedName("size")
    private String pictureSize;

    @SerializedName("display_time")
    private String pictureDisplayTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public String getPictureExt() {
        return pictureExt;
    }

    public void setPictureExt(String pictureExt) {
        this.pictureExt = pictureExt;
    }

    public String getPictureSize() {
        return pictureSize;
    }

    public void setPictureSize(String pictureSize) {
        this.pictureSize = pictureSize;
    }

    public String getPictureDisplayTime() {
        return pictureDisplayTime;
    }

    public void setPictureDisplayTime(String pictureDisplayTime) {
        this.pictureDisplayTime = pictureDisplayTime;
    }
}

package com.thecn.app.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.thecn.app.models.User.User;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by pheebner on 12/23/13.
 */
public class Reflection implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("text")
    private String text;

    @SerializedName("user")
    private User user;

    @SerializedName("is_owner")
    private boolean isOwner;

    @SerializedName("count")
    private ContentCount count;

    @SerializedName("has_set_good")
    private int isLiked;

    @SerializedName("has_set_best")
    private int isBest;

    @SerializedName("display_time")
    private String displayTime;

    @SerializedName("parent_id")
    private String parentId;

    private transient CharSequence processedText;

    public CharSequence getProcessedText() {
        return processedText;
    }

    public void setProcessedText(CharSequence processedText) {
        this.processedText = processedText;
    }

    public String getId() {
        return id;
    }

    public void setId(String postId) {
        this.id = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public int getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(int isLiked) {
        this.isLiked = isLiked;
    }

    public int getIsBest() {
        return isBest;
    }

    public void setIsBest(int isBest) {
        this.isBest = isBest;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }
}

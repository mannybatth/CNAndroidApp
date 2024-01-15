package com.thecn.app.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Attachment implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String attachmentName;

    @SerializedName("view_url")
    private String attachmentURL;

    @SerializedName("extension")
    private String attachmentExt;

    @SerializedName("size")
    private String attachmentSize;

    @SerializedName("display_time")
    private String attachmentDisplayTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentURL() {
        return attachmentURL;
    }

    public void setAttachmentURL(String attachmentURL) {
        this.attachmentURL = attachmentURL;
    }

    public String getAttachmentExt() {
        return attachmentExt;
    }

    public void setAttachmentExt(String attachmentExt) {
        this.attachmentExt = attachmentExt;
    }

    public String getAttachmentSize() {
        return attachmentSize;
    }

    public void setAttachmentSize(String attachmentSize) {
        this.attachmentSize = attachmentSize;
    }

    public String getAttachmentDisplayTime() {
        return attachmentDisplayTime;
    }

    public void setAttachmentDisplayTime(String attachmentDisplayTime) {
        this.attachmentDisplayTime = attachmentDisplayTime;
    }
}

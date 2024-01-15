package com.thecn.app.models.Course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by philjay on 2/28/14.
 */
public class CourseSchool implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

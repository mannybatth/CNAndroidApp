package com.thecn.app.models.User;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by philjay on 3/5/14.
 */
public class UserCurrentWork implements Serializable {

    @SerializedName("company")
    private String company;

    @SerializedName("position")
    private String position;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public UserCurrentWork(String company, String position) {
        this.company = company;
        this.position = position;
    }
}

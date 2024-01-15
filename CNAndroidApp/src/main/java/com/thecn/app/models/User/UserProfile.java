package com.thecn.app.models.User;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONObject;

import java.io.Serializable;

public class UserProfile implements Serializable {

    @SerializedName("about")
    private String about;

    @SerializedName("birthday")
    private String birthday;

    @SerializedName("country_id")
    private String countryID;

    @SerializedName("gender")
    private String gender;

    @SerializedName("current_city_id")
    private String currentCityID;

    @SerializedName("display_about")
    private String displayAbout;

    @SerializedName("primary_email")
    private String primaryEmail;

    @SerializedName("primary_language_name")
    private String primaryLanguage;

    @SerializedName("secondary_email")
    private String secondaryEmail;

    @SerializedName("position")
    private UserPosition userPosition;

    @SerializedName("current_work")
    private UserCurrentWork userCurrentWork;

    @SerializedName("time_zone_name")
    private String timeZone;

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getCountryID() {
        return countryID;
    }

    public void setCountryID(String countryID) {
        this.countryID = countryID;
    }

    public String getCurrentCityID() {
        return currentCityID;
    }

    public void setCurrentCityID(String currentCityID) {
        this.currentCityID = currentCityID;
    }

    public String getDisplayAbout() {
        return displayAbout;
    }

    public void setDisplayAbout(String displayAbout) {
        this.displayAbout = displayAbout;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public String getSecondaryEmail() {
        return secondaryEmail;
    }

    public void setSecondaryEmail(String secondaryEmail) {
        this.secondaryEmail = secondaryEmail;
    }


    public UserPosition getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(UserPosition userPosition) {
        this.userPosition = userPosition;
    }


    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(String primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public UserCurrentWork getUserCurrentWork() {
        return userCurrentWork;
    }

    public void setUserCurrentWork(UserCurrentWork userCurrentWork) {
        this.userCurrentWork = userCurrentWork;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}

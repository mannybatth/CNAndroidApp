package com.thecn.app.models.User;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class UserPosition implements Serializable{

    @SerializedName("position")
    private String position;

    @SerializedName("school_name")
    private String schoolName;

    @SerializedName("type")
    private String type;

    @SerializedName("web_address")
    private String webAddress;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public UserPosition(String position, String schoolName, String type, String webAddress) {
        this.position = position;
        this.schoolName = schoolName;
        this.type = type;
        this.webAddress = webAddress;
    }

    public String getUserPositionString() {

        String positionContent = null;

        String position = getPosition();
        position = position != null ? position : "";

        String schoolName = getSchoolName();
        schoolName = schoolName != null ? schoolName : "";

        String type = getType();
        type = type != null ? type : "";

        if (position.equals("other")) {
            positionContent = type;
        } else {
            if (position.length() > 0) {
                String positionCaps = getCapitalizedString(position);
                String schoolNameCaps = getCapitalizedString(schoolName);

                positionContent =  positionCaps + " at " + schoolNameCaps;
            }
        }

        return positionContent;
    }

    private String getCapitalizedString(String string) {
        if (string.length() == 1) {
            return Character.toString(Character.toUpperCase(string.charAt(0)));
        } else if (string.length() > 1) {
            return Character.toUpperCase(string.charAt(0)) + string.substring(1);
        } else return string;
    }
}

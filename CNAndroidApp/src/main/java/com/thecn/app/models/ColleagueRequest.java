package com.thecn.app.models;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.ColleagueRequestStore;
import com.thecn.app.stores.ResponseCallback;

import java.io.Serializable;

/**
 * Created by philjay on 5/1/14.
 */
public class ColleagueRequest implements Serializable {

    @SerializedName("ctime")
    private double cTime;

    @SerializedName("display_time")
    private String displayTime;

    @SerializedName("model")
    private User user;

    @SerializedName("status")
    private int status;

    @SerializedName("type")
    private String type;

    private transient boolean actionTaken = false;
    private transient boolean accepted;

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
        actionTaken = true;
    }

    public boolean isActionTaken() {
        return actionTaken;
    }

    public double getcTime() {
        return cTime;
    }

    public void setcTime(double cTime) {
        this.cTime = cTime;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public interface Callback {
        public void onAccept();

        public void onReject();
    }

    public Callback getCallback(final ResponseCallback callback) {
        try {
            final String userID = user.getId();

            if (userID != null) {
                return new Callback() {
                    @Override
                    public void onAccept() {
                        ColleagueRequestStore.replyToRequest(true, userID, callback);
                    }

                    @Override
                    public void onReject() {
                        ColleagueRequestStore.replyToRequest(false, userID, callback);
                    }
                };
            }

        } catch (NullPointerException e) {
            // No user or user id set
        }

        return null;
    }
}

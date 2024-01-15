package com.thecn.app.models;

import java.io.Serializable;

/**
 * Created by philjay on 5/5/14.
 */
public class UserNewMessage implements Serializable {

    private int notificationCount;

    private int emailCount;

    private int colleagueRequestCount;

    public UserNewMessage(int notificationCount, int emailCount, int colleagueRequestCount) {
        this.notificationCount = notificationCount;
        this.emailCount = emailCount;
        this.colleagueRequestCount = colleagueRequestCount;
    }

    public UserNewMessage() {
        notificationCount = 0;
        emailCount = 0;
        colleagueRequestCount = 0;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }

    public int getEmailCount() {
        return emailCount;
    }

    public void setEmailCount(int emailCount) {
        this.emailCount = emailCount;
    }

    public int getColleagueRequestCount() {
        return colleagueRequestCount;
    }

    public void setColleagueRequestCount(int colleagueRequestCount) {
        this.colleagueRequestCount = colleagueRequestCount;
    }

    public int getTotal() {
        return notificationCount + emailCount + colleagueRequestCount;
    }

    public static UserNewMessage getCopy(UserNewMessage userNewMessage) {
        int notificationCount = userNewMessage.getNotificationCount();
        int emailCount = userNewMessage.getEmailCount();
        int requestCount = userNewMessage.getColleagueRequestCount();

        return new UserNewMessage(notificationCount, emailCount, requestCount);
    }
}

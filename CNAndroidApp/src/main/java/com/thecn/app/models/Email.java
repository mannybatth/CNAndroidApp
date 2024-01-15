package com.thecn.app.models;

import android.provider.Telephony;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.User.User;
import com.thecn.app.tools.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by philjay on 4/23/14.
 */
public class Email implements Serializable {
    @SerializedName("content")
    private String content;

    @SerializedName("display_time")
    private String displayTime;

    @SerializedName("id")
    private String id;

    @SerializedName("is_reply_email")
    private Boolean isReply;

    @SerializedName("parent_email_id")
    private String parentId;

    @SerializedName("is_sender")
    private Boolean isSender;

    @SerializedName("is_unread")
    private Boolean isUnread;

    @SerializedName("subject")
    private String subject;

    @SerializedName("type")
    private String type;

    @SerializedName("sub_emails")
    private ArrayList<Email> subEmails;

    @SerializedName("sender")
    private Address serializedSender;

    @SerializedName("receivers")
    private ArrayList<Address> serializedReceivers;

    @SerializedName("reply_type")
    private String replyType;

    @SerializedName("origin")
    private Email origin;

    public Email getOrigin() {
        return origin;
    }

    public void setOrigin(Email origin) {
        this.origin = origin;
    }

    public String getReplyType() {
        return replyType;
    }

    public void setReplyType(String replyType) {
        this.replyType = replyType;
    }

    public Address getSerializedSender() {
        return serializedSender;
    }

    public void setSerializedSender(Address serializedSender) {
        this.serializedSender = serializedSender;
    }

    public ArrayList<Address> getSerializedReceivers() {
        return serializedReceivers;
    }

    public void setSerializedReceivers(ArrayList<Address> serializedReceivers) {
        this.serializedReceivers = serializedReceivers;
    }

    private ArrayList<Address> nonMemberRecipients;

    public ArrayList<Address> getNonMemberRecipients() {
        return nonMemberRecipients;
    }

    public void setNonMemberRecipients(ArrayList<Address> nonMemberRecipients) {
        this.nonMemberRecipients = nonMemberRecipients;
    }

    public static class Address implements Serializable {
        @SerializedName("id")
        private String id;

        @SerializedName("type")
        private String type;

        @SerializedName("receive_type")
        private String receiveType;

        private transient User user;

        public Address(String id, String type) {
            this.id = id;
            this.type = type;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        public void setReceiveType(String receiveType) {
            this.receiveType = receiveType;
        }

        public String getReceiveType() {
            return receiveType;
        }
    }

    private transient ArrayList<User> toUsers;

    private transient ArrayList<User> ccUsers;

    private transient User sender;

    private transient Course course;

    private transient Conexus conexus;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isReply() {
        return isReply;
    }

    public void setReply(boolean isReply) {
        this.isReply = isReply;
    }

    public boolean isSender() {
        return isSender;
    }

    public void setSender(boolean isSender) {
        this.isSender = isSender;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean isUnread) {
        this.isUnread = isUnread;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Email> getSubEmails() {
        return subEmails;
    }

    public void setSubEmails(ArrayList<Email> subEmails) {
        this.subEmails = subEmails;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public ArrayList<User> getToUsers() {
        return toUsers;
    }

    public void setToUsers(ArrayList<User> toUsers) {
        this.toUsers = toUsers;
    }

    public ArrayList<User> getCCUsers() {
        return ccUsers;
    }

    public void setCCUsers(ArrayList<User> ccUsers) {
        this.ccUsers = ccUsers;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Conexus getConexus() {
        return conexus;
    }

    public void setConexus(Conexus conexus) {
        this.conexus = conexus;
    }

    public static Address getAddressFromUser(User user) throws NullPointerException{
        String id = user.getId();
        if (id == null) {
            throw mUserIDNullPointer;
        }

        return new Email.Address(id, "user");
    }

    private static final NullPointerException mUserIDNullPointer = new NullPointerException("User ID should not be null");
}

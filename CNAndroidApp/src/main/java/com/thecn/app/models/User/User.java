package com.thecn.app.models.User;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.models.Avatar;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Country;
import com.thecn.app.models.Course.Course;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("cn_number")
    private String CNNumber;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("avatar")
    private Avatar avatar;

    @SerializedName("country")
    private Country country;

    @SerializedName("courses")
    private ArrayList<Course> courses;

    @SerializedName("conexuses")
    private ArrayList<Conexus> conexuses;

    @SerializedName("profile")
    private UserProfile userProfile;

    @SerializedName("score")
    private Score score;

    @SerializedName("receive_type")
    private String receiveType;

    @SerializedName("relations")
    private Relations relations;

    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCNNumber() {
        return CNNumber;
    }

    public void setCNNumber(String CNNumber) {
        this.CNNumber = CNNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<Course> courses) {
        this.courses = courses;
    }

    public ArrayList<Conexus> getConexuses() {
        return conexuses;
    }

    public void setConexuses(ArrayList<Conexus> conexuses) {
        this.conexuses = conexuses;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public String getReceiveType() {
        return receiveType;
    }

    public void setReceiveType(String receiveType) {
        this.receiveType = receiveType;
    }

    public Relations getRelations() {
        return relations;
    }

    public void setRelations(Relations relations) {
        this.relations = relations;
    }

    public static class Relations implements Serializable {
        @SerializedName("is_my_colleague_user")
        private boolean colleague;

        @SerializedName("is_my_follower_user")
        private boolean follower;

        @SerializedName("is_my_following_user")
        private boolean following;

        @SerializedName("is_my_passive_colleague_user")
        private boolean passiveColleague;

        @SerializedName("is_my_pending_colleague_user")
        private boolean pendingColleague;

        @SerializedName("is_myself")
        private boolean myself;

        public boolean isColleague() {
            return colleague;
        }

        public void setColleague(boolean colleague) {
            this.colleague = colleague;
        }

        public boolean isFollower() {
            return follower;
        }

        public void setFollower(boolean follower) {
            this.follower = follower;
        }

        public boolean isFollowing() {
            return following;
        }

        public void setFollowing(boolean following) {
            this.following = following;
        }

        public boolean isPassiveColleague() {
            return passiveColleague;
        }

        public void setPassiveColleague(boolean passiveColleague) {
            this.passiveColleague = passiveColleague;
        }

        public boolean isPendingColleague() {
            return pendingColleague;
        }

        public void setPendingColleague(boolean pendingColleague) {
            this.pendingColleague = pendingColleague;
        }

        public boolean isMyself() {
            return myself;
        }

        public void setMyself(boolean myself) {
            this.myself = myself;
        }
    }
}

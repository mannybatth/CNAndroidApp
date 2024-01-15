package com.thecn.app.models.Course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Course implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("course_id")
    private String courseNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("about")
    private String about;

    @SerializedName("display_time")
    private String displayTime;

    @SerializedName("logo_url")
    private String logoURL;

    @SerializedName("type")
    private String type;

    @SerializedName("user_position")
    private String userPosition;

    @SerializedName("is_start")
    private boolean isStart;

    @SerializedName("is_end")
    private boolean isEnd;

    @SerializedName("tasks")
    private ArrayList<Task> tasks;

    @SerializedName("school")
    private CourseSchool school;

    @SerializedName("instructor_users")
    private InstructorUsers instructorUsers;

    @SerializedName("score")
    private Score score;

    @SerializedName("score_expected_today")
    private int expectedScore;

    @SerializedName("score_setting")
    private ScoreSetting scoreSetting;

    @SerializedName("user_score")
    private UserScore userScore;

    @SerializedName("most_course_score_users")
    private ArrayList<ScoreUser> mostScoreUsers;

    @SerializedName("least_course_score_users")
    private ArrayList<ScoreUser> leastScoreUsers;

    @SerializedName("count")
    private Count count;

    public Course(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition;
    }

    public boolean getIsStart() {
        return isStart;
    }

    public void setIsStart(boolean isStart) {
        this.isStart = isStart;
    }

    public boolean getIsEnd() {
        return isEnd;
    }

    public void setIsEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public CourseSchool getSchool() {
        return school;
    }

    public void setSchool(CourseSchool school) {
        this.school = school;
    }


    public InstructorUsers getInstructorUsers() {
        return instructorUsers;
    }

    public void setInstructorUsers(InstructorUsers instructorUsers) {
        this.instructorUsers = instructorUsers;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public int getExpectedScore() {
        return expectedScore;
    }

    public void setExpectedScore(int expectedScore) {
        this.expectedScore = expectedScore;
    }

    public ScoreSetting getScoreSetting() {
        return scoreSetting;
    }

    public void setScoreSetting(ScoreSetting scoreSetting) {
        this.scoreSetting = scoreSetting;
    }

    public UserScore getUserScore() {
        return userScore;
    }

    public void setUserScore(UserScore userScore) {
        this.userScore = userScore;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean isStart) {
        this.isStart = isStart;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public ArrayList<ScoreUser> getMostScoreUsers() {
        return mostScoreUsers;
    }

    public void setMostScoreUsers(ArrayList<ScoreUser> mostScoreUsers) {
        this.mostScoreUsers = mostScoreUsers;
    }

    public ArrayList<ScoreUser> getLeastScoreUsers() {
        return leastScoreUsers;
    }

    public void setLeastScoreUsers(ArrayList<ScoreUser> leastScoreUsers) {
        this.leastScoreUsers = leastScoreUsers;
    }

    public Count getCount() {
        return count;
    }

    public void setCount(Count count) {
        this.count = count;
    }

    public static String[] getIds(ArrayList<Course> list) {

        String[] ids = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            ids[i] = list.get(i).getId();
        }

        return ids;
    }

}

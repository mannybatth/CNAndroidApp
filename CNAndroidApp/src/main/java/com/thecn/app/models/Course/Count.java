package com.thecn.app.models.Course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by philjay on 4/9/14.
 */
public class Count implements Serializable {
    @SerializedName("all")
    int allMemberCount;

    @SerializedName("instructor")
    int instructorCount;

    @SerializedName("student")
    int studentCount;

    public int getAllMemberCount() {
        return allMemberCount;
    }

    public void setAllMemberCount(int allMemberCount) {
        this.allMemberCount = allMemberCount;
    }

    public int getInstructorCount() {
        return instructorCount;
    }

    public void setInstructorCount(int instructorCount) {
        this.instructorCount = instructorCount;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }
}

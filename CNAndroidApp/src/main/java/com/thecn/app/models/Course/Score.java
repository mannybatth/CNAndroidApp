package com.thecn.app.models.Course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by philjay on 4/3/14.
 */
public class Score implements Serializable {

    @SerializedName("total")
    private double total;

    @SerializedName("average")
    private int average;

    @SerializedName("average_student")
    private int studentAverage;

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getAverage() {
        return average;
    }

    public void setAverage(int average) {
        this.average = average;
    }

    public int getStudentAverage() {
        return studentAverage;
    }

    public void setStudentAverage(int studentAverage) {
        this.studentAverage = studentAverage;
    }
}

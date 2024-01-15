package com.thecn.app.models.User;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by philjay on 4/15/14.
 */
public class Score implements Serializable {
    @SerializedName("total_seeds")
    private double totalSeeds;

    @SerializedName("total")
    private int total;

    public double getTotalSeeds() {
        return totalSeeds;
    }

    public void setTotalSeeds(double totalSeeds) {
        this.totalSeeds = totalSeeds;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}

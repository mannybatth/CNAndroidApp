package com.thecn.app.models.Conexus;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by philjay on 4/15/14.
 */
public class UserScore implements Serializable {

    @SerializedName("sub_total_seeds")
    private double subTotalSeeds;

    @SerializedName("sub_total")
    private int subTotal;

    public double getSubTotalSeeds() {
        return subTotalSeeds;
    }

    public void setSubTotalSeeds(double subTotalSeeds) {
        this.subTotalSeeds = subTotalSeeds;
    }

    public int getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(int subTotal) {
        this.subTotal = subTotal;
    }
}

package com.thecn.app.models.Course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by philjay on 4/3/14.
 */
public class ScoreSetting implements Serializable {

    @SerializedName("due_date")
    private String dueDate;

    @SerializedName("gradebook_item_type")
    private String gradebookItemType;

    @SerializedName("percentage")
    private String percentage;

    @SerializedName("required_number")
    private int requiredNumber;

    @SerializedName("use")
    private String use;

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getGradebookItemType() {
        return gradebookItemType;
    }

    public void setGradebookItemType(String gradebookItemType) {
        this.gradebookItemType = gradebookItemType;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public int getRequiredNumber() {
        return requiredNumber;
    }

    public void setRequiredNumber(int requiredNumber) {
        this.requiredNumber = requiredNumber;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }
}

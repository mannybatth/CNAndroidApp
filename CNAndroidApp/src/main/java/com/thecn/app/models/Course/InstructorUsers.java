package com.thecn.app.models.Course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by philjay on 3/4/14.
 */
public class InstructorUsers implements Serializable {

    @SerializedName("instructors")
    private ArrayList<Instructor> instructors;

    public ArrayList<Instructor> getInstructors() {
        return instructors;
    }

    public void setInstructors(ArrayList<Instructor> instructors) {
        this.instructors = instructors;
    }

}

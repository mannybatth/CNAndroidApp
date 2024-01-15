package com.thecn.app.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.NavigableMap;

/**
 * Created by philjay on 5/15/14.
 */
public class PostingGroup implements Serializable {

    public transient static final PostingGroup allMembers = new PostingGroup("public", "All CN Members");
    public transient static final PostingGroup myColleagues = new PostingGroup("my_colleague", "My Colleagues");
    public transient static final PostingGroup myFollowers = new PostingGroup("my_follower", "My Followers");
    public transient static final PostingGroup onlyMe = new PostingGroup("only_me", "Only Me");

    public transient static final PostingGroup course = new PostingGroup("course", "Course");
    public transient static final PostingGroup conexus = new PostingGroup("conexus", "Conexus");

    public static final PostingGroup[] allGroups = {allMembers, myColleagues, myFollowers, onlyMe};

    String mId;
    String mName;

    public PostingGroup(String id, String name) {
        mId = id;
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setName(String name) {
        mName = name;
    }

    public static String[] getIds(ArrayList<PostingGroup> list) {

        String[] ids = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            ids[i] = list.get(i).getId();
        }

        return ids;
    }
}

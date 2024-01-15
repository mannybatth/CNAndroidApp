package com.thecn.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.MyActivities.MyListActivity;
import com.thecn.app.adapters.PostVisibilityAdapter;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.PostingGroup;

import java.util.ArrayList;

public class PostVisibilityActivity extends MyListActivity {

    public static final String TAG = PostVisibilityActivity.class.getSimpleName();

    private PostVisibilityAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new PostVisibilityAdapter(this);

        setContentView(R.layout.activity_post_visibility);
        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        setListAdapter(adapter);

        setCheckedItemsFromIntent();

        Button updateButton = (Button) findViewById(R.id.update_visibility);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateVisibility();
            }
        });
    }

    private void setCheckedItemsFromIntent() {

        ArrayList<PostingGroup> visibleGroups = (ArrayList<PostingGroup>) getIntent().getSerializableExtra("GROUPS");
        ArrayList<Course> courses = (ArrayList<Course>) getIntent().getSerializableExtra("COURSES");
        ArrayList<Conexus> conexuses = (ArrayList<Conexus>) getIntent().getSerializableExtra("CONEXUSES");

        ListView lv = getListView();

        for (PostingGroup group : visibleGroups) {
            int index = adapter.getItem(group);

            if (index >= 0) lv.setItemChecked(index, true);
        }

        for (Course course : courses) {
            int index = adapter.getItem(course);

            if (index >= 0) lv.setItemChecked(index, true);
        }

        for (Conexus conexus : conexuses) {
            int index = adapter.getItem(conexus);

            if (index >= 0) lv.setItemChecked(index, true);
        }
    }

    private void updateVisibility() {

        PostVisibilityItemCollection collection = getCheckedItems();

        if (collection.getTotalItems() > 0) {
            ArrayList<PostingGroup> visibleGroups = collection.getGroups();
            ArrayList<Course> courses = collection.getCourses();
            ArrayList<Conexus> conexuses = collection.getConexuses();

            ArrayList<PostingGroup> invisibleGroups = new ArrayList<PostingGroup>();

            if (courses.size() > 0) invisibleGroups.add(PostingGroup.course);
            if (conexuses.size() > 0) invisibleGroups.add(PostingGroup.conexus);

            Intent intent = new Intent();
            intent.putExtra("V_GROUPS", visibleGroups);
            intent.putExtra("INV_GROUPS", invisibleGroups);
            intent.putExtra("COURSES", courses);
            intent.putExtra("CONEXUSES", conexuses);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            AppSession.showToast("At least one item must be selected.");
        }
    }

    public class PostVisibilityItemCollection {
        public ArrayList<PostingGroup> mGroups;
        public ArrayList<Course> mCourses;
        public ArrayList<Conexus> mConexuses;

        public PostVisibilityItemCollection(ArrayList<PostingGroup> group,
                                            ArrayList<Course> courses,
                                            ArrayList<Conexus> conexuses) {
            mGroups = group;
            mCourses = courses;
            mConexuses = conexuses;
        }

        public int getTotalItems() {
            return mGroups.size() + mCourses.size() + mConexuses.size();
        }

        public ArrayList<PostingGroup> getGroups() {
            return mGroups;
        }

        public void setGroups(ArrayList<PostingGroup> mGroup) {
            this.mGroups = mGroup;
        }

        public ArrayList<Course> getCourses() {
            return mCourses;
        }

        public void setCourses(ArrayList<Course> mCourses) {
            this.mCourses = mCourses;
        }

        public ArrayList<Conexus> getConexuses() {
            return mConexuses;
        }

        public void setConexuses(ArrayList<Conexus> mConexuses) {
            this.mConexuses = mConexuses;
        }
    }

    public PostVisibilityItemCollection getCheckedItems() {
        ArrayList<PostingGroup> groups = new ArrayList<PostingGroup>();
        ArrayList<Course> courses = new ArrayList<Course>();
        ArrayList<Conexus> conexuses = new ArrayList<Conexus>();

        for (int i = 0; i < getListView().getCount(); i++) {
            if (getListView().isItemChecked(i)) {
                Object object = adapter.getItem(i);

                if (object instanceof PostingGroup) {
                    groups.add((PostingGroup) object);
                } else if (object instanceof Course) {
                    courses.add((Course) object);
                } else if (object instanceof Conexus) {
                    conexuses.add((Conexus) object);
                }
            }
        }

        return new PostVisibilityItemCollection(groups, courses, conexuses);
    }

    @Override
    public void onBackPressed() {
        AppSession.showToast("Settings not updated");
        super.onBackPressed();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {

        //all members
        if (position == 0 && l.isItemChecked(0))
            for (int i = 1; i < adapter.getCount(); i++)
                l.setItemChecked(i, false);

        //only me
        else if (position == 1 && l.isItemChecked(1))
            notGlobalOrOnlyMe(l);

        //my colleagues
        else if (position == 2 && l.isItemChecked(2))
            notGlobalOrOnlyMe(l);

        //my followers
        else if (position == 3 && l.isItemChecked(3)) {
            l.setItemChecked(0, false);
            l.setItemChecked(1, false);
            l.setItemChecked(2, false);

            for (int i = 4; i < adapter.getCount(); i++) {
                l.setItemChecked(i, false);
            }

        } else if (position > 3 && l.isItemChecked(position))
            notGlobalOrOnlyMe(l);
    }

    private void notGlobalOrOnlyMe(ListView lv) {
        lv.setItemChecked(0, false);
        lv.setItemChecked(3, false);
    }
}

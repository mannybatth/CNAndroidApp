package com.thecn.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.PostingGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostVisibilityAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {

    private List<Object> mItems;
    private int[] mSectionIndices;
    private String[] mSectionTitles = {"Courses", "Conexus"};
    public LayoutInflater mInflater;

    public PostVisibilityAdapter(Context context) {
        mInflater = LayoutInflater.from(context);

        mItems = new ArrayList<Object>();
        mSectionIndices = new int[3];

        loadList();
    }

    public void loadList() {
        mItems.clear();
        PostingGroup[] groups = PostingGroup.allGroups;
        mItems.addAll(Arrays.asList(groups));

        ArrayList<Course> courses = AppSession.getInstance().getSimpleUserCourses();
        int coursesSize;
        if (courses != null) {
            coursesSize = courses.size();
            mItems.addAll(courses);
        } else {
            coursesSize = 0;
        }

        ArrayList<Conexus> conexuses = AppSession.getInstance().getSimpleUserConexuses();
        if (conexuses != null) mItems.addAll(conexuses);

        mSectionIndices[0] = 0;
        mSectionIndices[1] = 4;
        mSectionIndices[2] = coursesSize + mSectionIndices[1];

        notifyDataSetChanged();
    }

    public int getItem(PostingGroup group) {
        for (int i = mSectionIndices[0]; i < mSectionIndices[1]; i++) {
            Object object = getItem(i);

            if (object instanceof PostingGroup) {
                String id1 = group.getId();
                String id2 = ((PostingGroup) object).getId();

                if (id1.equals(id2)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public int getItem(Course course) {
        for (int i = mSectionIndices[1]; i < mSectionIndices[2]; i++) {
            Object object = getItem(i);

            if (object instanceof Course) {
                String id1 = course.getId();
                String id2 = ((Course) object).getId();

                if (id1.equals(id2)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public int getItem(Conexus conexus) {
        for (int i = mSectionIndices[2]; i < mItems.size(); i++) {
            Object object = getItem(i);

            if (object instanceof Conexus) {
                String id1 = conexus.getId();
                String id2 = ((Conexus) object).getId();

                if (id1.equals(id2)) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CheckedTextView checkedTextView;

        if (convertView == null) {
            checkedTextView = (CheckedTextView) mInflater.inflate(R.layout.post_visibility_list_item, parent, false);
        } else {
            checkedTextView = (CheckedTextView) convertView;
        }

        Object item = getItem(position);

        if (item instanceof Course) {

            Course course = (Course) item;

            checkedTextView.setText(course.getName());
        } else if (item instanceof Conexus) {

            Conexus conexus = (Conexus) item;

            checkedTextView.setText(conexus.getName());
        } else if (item instanceof PostingGroup) {

            PostingGroup group = (PostingGroup) item;

            checkedTextView.setText(group.getName());
        }

        return checkedTextView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        TextView textView;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.post_visibility_list_header, parent, false);
            textView = (TextView) convertView.findViewById(R.id.list_header_title);
            convertView.setTag(textView);
        } else {
            textView = (TextView) convertView.getTag();
        }

        if (getItem(position) instanceof Course) {
            textView.setVisibility(View.VISIBLE);
            textView.setText("Courses");
        } else if (getItem(position) instanceof Conexus) {
            textView.setVisibility(View.VISIBLE);
            textView.setText("Conexus");
        } else {
            textView.setText("");
            textView.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        if (getItem(position) instanceof Course) {
            return 2;
        } else if (getItem(position) instanceof Conexus) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getPositionForSection(int section) {
        if (section >= mSectionIndices.length) {
            section = mSectionIndices.length - 1;
        } else if (section < 0) {
            section = 0;
        }

        return mSectionIndices[section];
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < mSectionIndices.length; i++) {
            if (position < mSectionIndices[i]) {
                return i - 1;
            }
        }

        return mSectionIndices.length - 1;
    }

    @Override
    public Object[] getSections() {
        return mSectionTitles;
    }

}  
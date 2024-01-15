package com.thecn.app.fragments.Course;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.R;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.Course.CourseSchool;
import com.thecn.app.models.Course.Instructor;
import com.thecn.app.models.Course.InstructorUsers;
import com.thecn.app.tools.MyVolley;

import java.util.ArrayList;

public class CourseAboutFragment extends Fragment{

    public static final String TAG = CourseAboutFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_COURSE_KEY = "course";

    private Course mCourse;

    private ImageLoader imageLoader = MyVolley.getImageLoader();

    public static CourseAboutFragment newInstance(Course mCourse) {
        CourseAboutFragment fragment = new CourseAboutFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_COURSE_KEY, mCourse);
        fragment.setArguments(args);

        return fragment;
    }

    public CourseAboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mCourse = (Course) getArguments().getSerializable(FRAGMENT_BUNDLE_COURSE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_about, container, false);

//        String avatarUrl = mCourse.getLogoURL() + ".w160.jpg";
//
//        ImageView mImageView = (ImageView) view.findViewById(R.id.avatarImg);
//        imageLoader.get(avatarUrl,
//                ImageLoader.getImageListener(mImageView,
//                        R.drawable.default_user_icon,
//                        R.drawable.default_user_icon));
//

//        TextView courseNameTxtView = (TextView) view.findViewById(R.id.course_name);
//        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
//        courseNameTxtView.setTypeface(typeface);
//        courseNameTxtView.setText(courseName);
//
//        CourseSchool school = mCourse.getSchool();
//        String schoolName = null;
//        if (school != null)  {
//            schoolName = school.getName();
//        }
//        TextView courseSchoolTxtView = (TextView) view.findViewById(R.id.course_school);
//        courseSchoolTxtView.setTypeface(typeface);
//        courseSchoolTxtView.setText(schoolName);
//

//        TextView courseIdTxtView = (TextView) view.findViewById(R.id.course_number);
//        courseIdTxtView.setTypeface(typeface);
//        courseIdTxtView.setText(courseNumber);


        String courseName = mCourse.getName();
        courseName = courseName != null ? courseName : "(No name found)";
        TextView nameContent = (TextView) view.findViewById(R.id.name_content);
        nameContent.setText(courseName);

        String courseNumber = mCourse.getCourseNumber();
        if (courseNumber == null) courseNumber = "";
        TextView courseNumberContent = (TextView) view.findViewById(R.id.course_number_content);
        courseNumberContent.setText(courseNumber);

        String courseStatus = getCourseStatus();
        TextView statusContent = (TextView) view.findViewById(R.id.status_content);
        statusContent.setText(courseStatus);


        CourseSchool school = mCourse.getSchool();
        String schoolName = null;
        if (school != null)  {
            schoolName = school.getName();
        }
        schoolName = schoolName == null ? "" : schoolName;
        TextView schoolContent = (TextView) view.findViewById(R.id.school_content);
        schoolContent.setText(schoolName);

        ArrayList<Instructor> instructors = getCourseInstructorList();

        TextView instructorLabel = (TextView) view.findViewById(R.id.instructor_label);
        if (instructors != null && instructors.size() > 1) {
            instructorLabel.setText("Instructors");
        }

        String instructorString = getCourseInstructorString(instructors);
        TextView instructorContent = (TextView) view.findViewById(R.id.instructor_content);
        instructorContent.setText(instructorString);

        String courseAbout = mCourse.getAbout();
        if (courseAbout != null) {
            ((TextView) view.findViewById(R.id.about_content))
                    .setText(Html.fromHtml(courseAbout));
        } else {
            view.findViewById(R.id.about_layout)
                    .setVisibility(View.GONE);
        }

        return view;
    }

    private String getCourseStatus() {
        boolean started = mCourse.getIsStart();
        boolean ended = mCourse.getIsEnd();

        if (started) {
            return "Active";
        } else if (ended) {
            return "Ended";
        } else {
            return "Unavailable";
        }
    }

    private ArrayList<Instructor> getCourseInstructorList() {
        InstructorUsers insObject = mCourse.getInstructorUsers();
        ArrayList<Instructor> instructors = null;

        if (insObject != null) {
            instructors = insObject.getInstructors();
        }

        return instructors;
    }

    private String getCourseInstructorString(ArrayList<Instructor> instructors) {
        String instructorString = "(no instructor found)";

        if (instructors != null && instructors.size() > 0) {
            instructorString = instructors.get(0).getDisplayName();

            for (int i = 1; i < instructors.size(); i ++) {
                instructorString += ", " + instructors.get(i).getDisplayName();
            }
        }

        return instructorString;
    }
}

package com.thecn.app.fragments.Course;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.NavigationActivity;
import com.thecn.app.fragments.BasePostListFragment;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.Course.CourseSchool;
import com.thecn.app.models.Post;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.PostStore;
import com.thecn.app.tools.AnarBar;
import com.thecn.app.tools.MyVolley;


public class CoursePostsFragment extends BasePostListFragment {
    public static final String TAG = CoursePostsFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_COURSE_KEY = "course";

    private Course mCourse;
    AnarBar anarBar;

    private View headerView;

    ImageLoader imageLoader = MyVolley.getImageLoader();

    public static CoursePostsFragment newInstance(Course mCourse) {
        CoursePostsFragment fragment = new CoursePostsFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_COURSE_KEY, mCourse);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCourse = (Course) getArguments().getSerializable(FRAGMENT_BUNDLE_COURSE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        headerView = getLayoutInflater(savedInstanceState).inflate(R.layout.course_header, null);
        return inflater.inflate(R.layout.fragment_post_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().addHeaderView(headerView, null, false);

        setUpHeaderView();

        addPostButton(R.id.post_button, R.id.header_post_button);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        position --;
        super.onListItemClick(l, v, position, id);
    }

    private void setUpHeaderView() {
        String avatarUrl = mCourse.getLogoURL() + ".w160.jpg";

        ImageView mImageView = (ImageView) headerView.findViewById(R.id.avatarImg);
        imageLoader.get(avatarUrl,
                ImageLoader.getImageListener(mImageView,
                        R.drawable.default_user_icon,
                        R.drawable.default_user_icon));

        String courseName = mCourse.getName();
        courseName = courseName != null ? courseName : "(No name found)";
        TextView courseNameTxtView = (TextView) headerView.findViewById(R.id.course_name);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        courseNameTxtView.setTypeface(typeface);
        courseNameTxtView.setText(courseName);

        CourseSchool school = mCourse.getSchool();
        String schoolName = null;
        if (school != null)  {
            schoolName = school.getName();
        }
        TextView courseSchoolTxtView = (TextView) headerView.findViewById(R.id.course_school);
        courseSchoolTxtView.setTypeface(typeface);
        courseSchoolTxtView.setText(schoolName);

        String courseNumber = mCourse.getCourseNumber();
        if (courseNumber == null) courseNumber = "";
        TextView courseIdTxtView = (TextView) headerView.findViewById(R.id.course_number);
        courseIdTxtView.setTypeface(typeface);
        courseIdTxtView.setText(courseNumber);


        anarBar = new AnarBar(headerView.findViewById(R.id.anar_bar_parent),
                mCourse);
        addAnarBarCallbacks();
    }

    private void addAnarBarCallbacks() {
        try {
            final User user = AppSession.getInstance().getUser();

            if (user != null) {
                anarBar.setOnUserClick(new AnarBar.ImageCallback() {
                    @Override
                    public void onImageClick() {
                        ((NavigationActivity) getActivity())
                                .openProfilePage(user);
                    }
                });
            }
        } catch (NullPointerException e) {
            //something wrong
        }

        try {
            final User topUser = anarBar.getTopUserModel();

            if (topUser != null) {
                anarBar.setOnTopUserClick(new AnarBar.ImageCallback() {
                    @Override
                    public void onImageClick() {
                        ((NavigationActivity) getActivity())
                                .openProfilePage(topUser);
                    }
                });
            }
        } catch (NullPointerException e) {
            //something wrong
        }
    }

    @Override
    public void onPostAdded(Post post, String[] ids) {
        String thisId = TAG + mCourse.getId();
        for (String id : ids) {
            if (id.equals(thisId)) {
                super.onPostAdded(post, ids);
                break;
            }
        }
    }

    public void loadPosts() {
        PostStore.getPostsFromCourse(mCourse.getId(), getLimit(), getOffset(), new PostsCallback(getHandler()));
    }

    public String getFragmentID() {
        return TAG + mCourse.getId();
    }
}

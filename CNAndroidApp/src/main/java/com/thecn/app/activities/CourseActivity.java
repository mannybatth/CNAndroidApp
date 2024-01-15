package com.thecn.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.thecn.app.AppSession;
import com.thecn.app.fragments.Course.CourseAboutFragment;
import com.thecn.app.fragments.Course.CoursePostsFragment;
import com.thecn.app.fragments.Course.CourseRosterFragment;
import com.thecn.app.fragments.Course.CourseTasksFragment;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.models.Course.Course;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;

import org.json.JSONObject;

public class CourseActivity extends ContentPageActivity {

    private Course mCourse;
    private String courseID;

    public static final int ABOUT_FRAGMENT = 0;
    public static final int TASKS_FRAGMENT = 1;
    public static final int ROSTER_FRAGMENT = 2;

    private static final String mLoadCourseFragmentTag = "load_course";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            try {
                mCourse = (Course) getIntent().getSerializableExtra("course");
                courseID = mCourse.getId();
                if (courseID == null) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException e) {
                onLoadingError();
                return;
            }

            LoadCourseFragment fragment = new LoadCourseFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(fragment, mLoadCourseFragmentTag)
                    .commit();

            fragment.loadCourse(courseID);

        } else {
            LoadCourseFragment fragment =
                    (LoadCourseFragment) getSupportFragmentManager().findFragmentByTag(mLoadCourseFragmentTag);

            if (!fragment.loading) {
                mCourse = (Course) savedInstanceState.getSerializable("course");
            }

            hideProgressBar();
        }

        setActionBarAndTitle(mCourse.getName());
    }

    public static class LoadCourseFragment extends MyFragment {

        public boolean loading = false;

        public void loadCourse(String courseID) {
            loading = true;

            CourseStore.getCourseById(courseID, new ResponseCallback(getHandler()) {
                @Override
                public void onPreExecute() {
                    loading = false;
                }

                @Override
                public void onSuccess(JSONObject response) {
                    Course course = CourseStore.getData(response);
                    CourseActivity activity = getCourseActivity();

                    if (course != null) {
                        activity.setCourse(course);
                        activity.hideProgressBar();
                        activity.initFragments(CourseActivity.TASKS_FRAGMENT);
                    } else {
                        activity.onLoadingError();
                    }
                }

                @Override
                public void onFailure(JSONObject response) {
                    getCourseActivity().onLoadingError();
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
                    getActivity().finish();
                }
            });
        }

        private CourseActivity getCourseActivity() {
            return (CourseActivity) getActivity();
        }
    }

    public void setCourse(Course course) {
        mCourse = course;
    }

    private void onLoadingError() {
        AppSession.showDataLoadError("course");
        finish();
    }

    @Override
    public void pushCreatePostActivity() {
        final Intent intent = new Intent(this, CreatePostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("COURSE", mCourse);
        openPage(new OpenPageCallback() {
            @Override
            public void openActivity() {
                startActivityForResult(intent, CREATE_NEW_POST_REQUEST);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("course", mCourse);
    }

    @Override
    protected FragmentPackage getStaticFragmentPackage() {
        String fragmentKey = "COURSE_" + courseID + "_POSTS";
        return new FragmentPackage("POSTS", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return CoursePostsFragment.newInstance(mCourse);
            }
        });
    }

    @Override
    protected FragmentPackage[] getFragmentPackages() {
        FragmentPackage[] packages = new FragmentPackage[3];
        String fragmentKey;

        fragmentKey = "COURSE_" + courseID + "_ABOUT";
        packages[ABOUT_FRAGMENT] = new FragmentPackage("ABOUT", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return CourseAboutFragment.newInstance(mCourse);
            }
        });

        fragmentKey = "COURSE_" + courseID + "_TASKS";
                packages[TASKS_FRAGMENT] = new FragmentPackage("TASKS", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return CourseTasksFragment.newInstance(mCourse);
            }
        });

        fragmentKey = "COURSE_" + courseID + "_ROSTER";
                packages[ROSTER_FRAGMENT] = new FragmentPackage("ROSTER", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return CourseRosterFragment.newInstance(mCourse);
            }
        });

        return packages;
    }

    @Override
    public void openCoursePage(Course course) {
        // dont open duplicate course page
        if (!mCourse.getId().equals(course.getId())) {
            super.openCoursePage(course);
        } else {
            closeNotificationDrawer();
        }
    }
}

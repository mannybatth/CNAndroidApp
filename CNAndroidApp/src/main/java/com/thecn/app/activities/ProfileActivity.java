package com.thecn.app.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.fragments.Profile.ProfileAboutFragment;
import com.thecn.app.fragments.Profile.ProfilePostsFragment;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;

import org.json.JSONObject;

public class ProfileActivity extends ContentPageActivity {

    private User mUser;
    private String mUserId;

    public static final int ABOUT_FRAGMENT = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            try {
                mUser = (User) getIntent().getSerializableExtra("user");
                if (mUser == null) {
                    String cnNumber = getIntent().getStringExtra("cn_number");
                    if (cnNumber == null) {
                        throw new NullPointerException();
                    }

                    LoadProfileFragment fragment = initLoadingFragment();
                    fragment.loadProfileByCNNumber(cnNumber);

                } else {
                    mUserId = mUser.getId();
                    if (mUserId == null) {
                        throw new NullPointerException();
                    }

                    LoadProfileFragment fragment = initLoadingFragment();
                    fragment.loadProfileByID(mUserId);
                }
            } catch (NullPointerException e) {
                onLoadingError();
                return;
            }

        } else {
            LoadProfileFragment fragment =
                    (LoadProfileFragment) getSupportFragmentManager().findFragmentByTag(mLoadProfileFragmentTag);

            if (!fragment.loading) {
                mUser = (User) savedInstanceState.getSerializable("user");
            }

            hideProgressBar();
        }

        if (mUser != null) {
            setActionBarAndTitle(mUser.getDisplayName() + "'s Profile");
        }
    }

    private static final String mLoadProfileFragmentTag = "load_profile";

    private LoadProfileFragment initLoadingFragment() {
        LoadProfileFragment fragment = new LoadProfileFragment();

        getSupportFragmentManager().beginTransaction()
                .add(fragment, mLoadProfileFragmentTag)
                .commit();

        return fragment;
    }

    public static class LoadProfileFragment extends MyFragment {

        public boolean loading = false;

        private ResponseCallback mUserCallback = new ResponseCallback() {
            @Override
            public void onPreExecute() {
                loading = false;
            }

            @Override
            public void onSuccess(JSONObject response) {
                User user = UserStore.getData(response);
                ProfileActivity activity = getProfileActivity();

                if (user != null) {
                    activity.setUser(user);
                    activity.hideProgressBar();
                    activity.initFragments(ProfileActivity.ABOUT_FRAGMENT);
                    activity.setActionBarAndTitle(user.getDisplayName() + "'s Profile");
                }
            }

            @Override
            public void onError(Exception error) {
                StoreUtil.showExceptionMessage(error);
                getProfileActivity().finish();
            }
        };

        public void loadProfileByID(String userID) {
            loading = true;

            UserStore.getUserById(userID, mUserCallback);
        }

        public void loadProfileByCNNumber(String cnNumber) {
            loading = true;

            UserStore.getUserByCNNumber(cnNumber, "", new ResponseCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    User user = UserStore.getData(response);

                    String userId;

                    try {
                        userId = user.getId();
                        if (userId == null) {
                            throw new NullPointerException();
                        } else {
                            //temporary fix, /cn_number/ api doesn't return "relations" object,
                            //so get user Id from cn number api, then use /user/ api to get user data
                            UserStore.getUserById(userId, mUserCallback);
                        }
                    } catch (NullPointerException e) {
                        mUserCallback.onError(new VolleyError("Could not get data."));
                    }
                }

                @Override
                public void onError(Exception error) {
                    mUserCallback.onError(error);
                }
            });
        }

        private ProfileActivity getProfileActivity() {
            return (ProfileActivity) getActivity();
        }
    }

    public void setUser(User user) {
        mUser = user;
    }

    private void onLoadingError() {
        AppSession.showDataLoadError("profile");
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("user", mUser);
    }

    @Override
    protected FragmentPackage getStaticFragmentPackage() {
        String fragmentKey = "PROFILE_" + mUserId + "_POSTS";
        return new FragmentPackage("POSTS", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return ProfilePostsFragment.newInstance(mUser);
            }
        });
    }

    @Override
    protected FragmentPackage[] getFragmentPackages() {
        FragmentPackage[] packages = new FragmentPackage[1];

        String fragmentKey = "PROFILE_" + mUserId + "_ABOUT";
        packages[ABOUT_FRAGMENT] = new FragmentPackage("ABOUT", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return ProfileAboutFragment.newInstance(mUser);
            }
        });
        return packages;
    }

    @Override
    public void openProfilePage(User user) {
        // dont open duplicate profile page
        if (!mUser.getId().equals(user.getId())) {
            super.openProfilePage(user);
        } else {
            closeNotificationDrawer();
        }
    }
}

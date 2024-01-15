package com.thecn.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.thecn.app.AppSession;
import com.thecn.app.fragments.Conexus.ConexusAboutFragment;
import com.thecn.app.fragments.Conexus.ConexusPostsFragment;
import com.thecn.app.fragments.Conexus.ConexusRosterFragment;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.stores.ConexusStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;

import org.json.JSONObject;

public class ConexusActivity extends ContentPageActivity {

    private Conexus mConexus;
    private String conexusID;

    public static final int ABOUT_FRAGMENT = 0;
    public static final int ROSTER_FRAGMENT = 1;

    private static final String mLoadConexusFragmentTag = "load_conexus";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            try {
                mConexus = (Conexus) getIntent().getSerializableExtra("conexus");
                conexusID = mConexus.getId();
            } catch (NullPointerException e) {
                onLoadingError();
                return;
            }

            LoadConexusFragment fragment = new LoadConexusFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(fragment, mLoadConexusFragmentTag)
                    .commit();

            fragment.loadConexus(conexusID);

        } else {
            LoadConexusFragment fragment =
                    (LoadConexusFragment) getSupportFragmentManager().findFragmentByTag(mLoadConexusFragmentTag);

            if (!fragment.loading) {
                mConexus = (Conexus) savedInstanceState.getSerializable("conexus");
            }

            hideProgressBar();
        }

        setActionBarAndTitle(mConexus.getName());
    }

    public static class LoadConexusFragment extends MyFragment {

        public boolean loading = false;

        public void loadConexus(String conexusID) {
            loading = true;

            ConexusStore.getConexusById(conexusID, new ResponseCallback(getHandler()) {
                @Override
                public void onPreExecute() {
                    loading = false;
                }

                @Override
                public void onSuccess(JSONObject response) {
                    Conexus conexus = ConexusStore.getData(response);
                    ConexusActivity activity = getConexusActivity();

                    if (conexus != null) {
                        activity.setConexus(conexus);
                        activity.hideProgressBar();
                        activity.initFragments(ConexusActivity.ABOUT_FRAGMENT);
                    } else {
                        activity.onLoadingError();
                    }
                }

                @Override
                public void onFailure(JSONObject response) {
                    getConexusActivity().onLoadingError();
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
                    getActivity().finish();
                }
            });

        }

        //convenience
        private ConexusActivity getConexusActivity() {
            return (ConexusActivity) getActivity();
        }
    }

    public void setConexus(Conexus conexus) {
        mConexus = conexus;
    }

    private void onLoadingError() {
        AppSession.showDataLoadError("conexus");
        finish();
    }

    @Override
    public void pushCreatePostActivity() {
        final Intent intent = new Intent(this, CreatePostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("CONEXUS", mConexus);
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
        outState.putSerializable("conexus", mConexus);
    }

    @Override
    protected FragmentPackage getStaticFragmentPackage() {
        String fragmentKey = "CONEXUS_" + conexusID + "_POSTS";
        return new FragmentPackage("POSTS", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return ConexusPostsFragment.newInstance(mConexus);
            }
        });
    }

    @Override
    protected FragmentPackage[] getFragmentPackages() {
        FragmentPackage[] packages = new FragmentPackage[2];
        String fragmentKey;

        fragmentKey = "CONEXUS_" + conexusID + "_ABOUT";
        packages[ABOUT_FRAGMENT] = new FragmentPackage("ABOUT", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return ConexusAboutFragment.newInstance(mConexus);
            }
        });

        fragmentKey = "CONEXUS_" + conexusID + "_ROSTER";
        packages[ROSTER_FRAGMENT] = new FragmentPackage("ROSTER", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return ConexusRosterFragment.newInstance(mConexus);
            }
        });

        return packages;
    }

    @Override
    public void openConexusPage(Conexus conexus) {
        // dont open duplicate conexus page
        if (!mConexus.getId().equals(conexus.getId())) {
            super.openConexusPage(conexus);
        } else {
            closeNotificationDrawer();
        }
    }
}

package com.thecn.app.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.fragments.EmailFragment;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.models.Email;
import com.thecn.app.stores.EmailStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by philjay on 4/24/14.
 */
public class EmailActivity extends NavigationActivity {

    private Email mEmail;

    private LoadEmailFragment mLoadEmailFragment;
    private static final String mLoadEmailFragmentTag = "load_email";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarAndTitle("Email");

        if(savedInstanceState == null) {
            String parentId;

            try {
                mEmail = (Email) getIntent().getSerializableExtra("email");

                if (mEmail.isReply()) {
                    parentId = mEmail.getParentId();
                } else {
                    parentId = mEmail.getId();
                }

                if (parentId == null) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException e) {
                onLoadingError();
                return;
            }

            mLoadEmailFragment = new LoadEmailFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(mLoadEmailFragment, mLoadEmailFragmentTag)
                    .commit();

            mLoadEmailFragment.loadEmail(parentId);

        } else {
            mLoadEmailFragment =
                    (LoadEmailFragment) getSupportFragmentManager().findFragmentByTag(mLoadEmailFragmentTag);

            if (!mLoadEmailFragment.loading) {
                mEmail = (Email) savedInstanceState.getSerializable("email");
            }

            hideProgressBar();
        }
    }

    public static class LoadEmailFragment extends MyFragment {

        public boolean loading = false;

        public void loadEmail(String parentID) {
            EmailStore.getEmailById(parentID, new ResponseCallback(getHandler()) {
                @Override
                public void onPreExecute() {
                    loading = false;
                }

                @Override
                public void onSuccess(JSONObject response) {
                    Email email = EmailStore.getData(response);
                    EmailActivity activity = getEmailActivity();

                    if (email != null) {
                        activity.setEmail(email);
                        activity.hideProgressBar();

                        FragmentManager manager = activity.getSupportFragmentManager();

                        //refresh the fragment when data is reloaded
                        Fragment fragment = manager.findFragmentByTag("email_fragment");
                        if (fragment != null) {
                            manager.beginTransaction().remove(fragment).commit();
                        }

                        fragment = EmailFragment.newInstance(email);
                        manager.beginTransaction()
                                .replace(R.id.container, fragment, "email_fragment")
                                .commit();
                    } else {
                        activity.onLoadingError();
                    }
                }

                @Override
                public void onFailure(JSONObject response) {
                    getEmailActivity().onLoadingError();
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
                    getActivity().finish();
                }
            });
        }

        private EmailActivity getEmailActivity() {
            return (EmailActivity) getActivity();
        }
    }

    private void onLoadingError() {
        AppSession.showDataLoadError("email");
        finish();
    }

    public void setEmail(Email email) {
        mEmail = email;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("email", mEmail);
    }

    @Override
    public void openEmailPage(Email email) {
        try {
            boolean samePage = false;
            boolean reload = false;

            String parentId;

            if (email.isReply()) {
                parentId = email.getParentId();
                if (parentId.equals(mEmail.getId())) {
                    samePage = true;

                    //Check if the email used to open the page
                    //is in the sub-email list of its parent.
                    //If not, then reload the fragment to get this new email
                    ArrayList<Email> subEmails = mEmail.getSubEmails();
                    reload = true;
                    if (subEmails != null) {
                        for (Email subEmail : subEmails) {
                            try {
                                if (subEmail.getId().equals(email.getId())) {
                                    reload = false;
                                    break;
                                }
                            } catch (NullPointerException e) {
                                //on error, treat as if subEmail did not equal
                            }
                        }
                    }
                }

            } else {
                parentId = email.getId();
                if (parentId.equals(mEmail.getId())) {
                    samePage = true;
                }
            }

            if (!samePage) {
                super.openEmailPage(email);
            } else {
                closeNotificationDrawer();
                if (reload) mLoadEmailFragment.loadEmail(parentId);
            }

        } catch (NullPointerException e) {
            // data not there...
        }
    }
}

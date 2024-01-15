package com.thecn.app.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.util.Rfc822Tokenizer;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.MyActivities.MyActionBarActivity;
import com.thecn.app.adapters.CNEmailRecipientAdapter;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.models.Email;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.EmailStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.views.CNEmailRecipientEditTextView;
import com.thecn.app.tools.GlobalGson;
import com.thecn.app.tools.MyVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class ComposeEmailActivity extends MyActionBarActivity {

    private ProgressDialog mProgressDialog;
    private CNEmailRecipientEditTextView mRecipientView, mCCView;
    private Toast mToast;

    private SendEmailFragment mSendEmailFragment;
    private static final String mSendEmailFragmentTag = "send_email_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_email);

        mRecipientView = (CNEmailRecipientEditTextView) findViewById(R.id.email_recipients);
        mRecipientView.setTokenizer(new Rfc822Tokenizer());
        CNEmailRecipientAdapter recipientAdapter = new CNEmailRecipientAdapter(this);
        mRecipientView.setAdapter(recipientAdapter);

        mCCView = (CNEmailRecipientEditTextView) findViewById(R.id.email_cc_recipients);
        mCCView.setTokenizer(new Rfc822Tokenizer());
        CNEmailRecipientAdapter ccAdapter = new CNEmailRecipientAdapter(this);
        mCCView.setAdapter(ccAdapter);

        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG); //did not forget to show
        mToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);

        mProgressDialog = new ProgressDialog(this) {
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                MyVolley.cancelRequests(EmailStore.SEND_EMAIL_TAG);
                showToast("Email not sent.");
                dismiss();
            }
        };
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Sending message...");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("New Email");

        if (savedInstanceState == null) {
            mSendEmailFragment = new SendEmailFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(mSendEmailFragment, mSendEmailFragmentTag)
                    .commit();
        } else {
            mSendEmailFragment =
                    (SendEmailFragment) getSupportFragmentManager().findFragmentByTag(mSendEmailFragmentTag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.compose_email, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        if (id == R.id.action_send_email) {
            mSendEmailFragment.sendEmail();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SendEmailFragment extends MyFragment {

        public void sendEmail() {
            JSONObject email = getComposeEmailActivity().constructEmail();

            if (email != null) {
                getComposeEmailActivity().showProgressDialog(true);

                ResponseCallback callback = new ResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        AppSession.showToast("Message sent");
                    }

                    @Override
                    public void onFailure(JSONObject response) {
                        AppSession.showToast("Could not send email.");
                    }

                    @Override
                    public void onError(Exception error) {
                        error = error != null ? error : new VolleyError("Could not use input");
                        StoreUtil.showExceptionMessage(error);
                    }

                    @Override
                    public void executeWithHandler(JSONObject response) {
                        getComposeEmailActivity().showProgressDialog(false);
                        if (getExecutionType() == ExecutionType.SUCCESS) {
                            getComposeEmailActivity().finish();
                        }
                    }
                };
                callback.setHandler(getHandler(), ResponseCallback.HANDLE_NONE);

                EmailStore.sendEmail(email, callback);
            }
        }

        private ComposeEmailActivity getComposeEmailActivity() {
            Activity activity = getActivity();
            if (activity != null && activity instanceof ComposeEmailActivity) {
                return (ComposeEmailActivity) activity;
            }

            return null;
        }
    }

    /**
     * constructs an email from the fields.  shows an error and returns null if something is wrong
     * @return an Email object
     */
    private JSONObject constructEmail() {

        ArrayList<Email.Address> recipients = new ArrayList<Email.Address>(mRecipientView.getRecipients());
        if (recipients.size() == 0) {
            showToast("Email must have recipients.");
            return null;
        }

        EditText subjectText = (EditText) findViewById(R.id.subject);
        Editable subject = subjectText.getText();
        if (subject == null || subject.length() == 0) {
            showToast("Subject cannot be blank.");
            return null;
        }

        EditText contentText = (EditText) findViewById(R.id.content);
        Editable content = contentText.getText();
        if (content == null || content.length() == 0) {
            showToast("Email content cannot be blank.");
            return null;
        }

        ArrayList<Email.Address> ccRecipients = mCCView.getRecipients();
        for (Email.Address address : ccRecipients) {
            address.setReceiveType("cc");
        }
        recipients.addAll(ccRecipients);

        HashMap<String, Object> email = new HashMap<String, Object>();
        email.put("receivers", recipients);
        email.put("sender", getSender());
        email.put("subject", subject.toString());
        email.put("content", content.toString());
        email.put("type", "normal");

        try {
            Gson gson = GlobalGson.getGson();
            return new JSONObject(gson.toJson(email));
        } catch (JSONException e) {
            return null;
        }
    }

    private Email.Address getSender() {
        User me = AppSession.getInstance().getUser();

        return new Email.Address(me.getId(), "user");
    }

    public void showProgressDialog(boolean show) {
        if (show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    private void showToast(String text) {
        mToast.setText(text);
        mToast.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("in_progress", mProgressDialog.isShowing());
        mProgressDialog.dismiss();
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean("in_progress")) {
            showProgressDialog(true);
        }
    }
}

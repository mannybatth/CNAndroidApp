package com.thecn.app.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.NavigationActivity;
import com.thecn.app.adapters.EmailAdapter;
import com.thecn.app.fragments.MyFragments.MyListFragment;
import com.thecn.app.models.Email;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.EmailStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CNNumberLinker;
import com.thecn.app.tools.GlobalGson;
import com.thecn.app.tools.InternalURLSpan;
import com.thecn.app.tools.MyVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class EmailFragment extends MyListFragment {

    public static final String TAG = EmailFragment.class.getSimpleName();

    private EmailAdapter adapter;
    private ArrayList<Email> mEmails;
    private View mHeaderView;

    private User mMe;

    private Email mParentEmail;

    private EditText mContentText;
    private Button mSubmitButton;

    public EmailFragment() {
        // Required empty public constructor
    }

    private static final String EMAIL_KEY = "email";

    public static EmailFragment newInstance(Email parentEmail) {
        EmailFragment fragment = new EmailFragment();
        Bundle args = new Bundle();
        args.putSerializable(EMAIL_KEY, parentEmail);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentEmail = (Email) getArguments().getSerializable(EMAIL_KEY);

        mEmails = mParentEmail.getSubEmails();
        if (mEmails != null) {
            Collections.reverse(mEmails);
        } else {
            mEmails = new ArrayList<Email>();
        }

        mHeaderView = getLayoutInflater(savedInstanceState)
                .inflate(R.layout.email_header, null);
        setUpHeader();

        mMe = AppSession.getInstance().getUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_email, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(null);

        ListView listView = getListView();
        listView.setDivider(null);
        listView.addHeaderView(mHeaderView);
        listView.setBackgroundColor(getResources().getColor(R.color.base_listview_background_color));

        mContentText = (EditText) view.findViewById(R.id.submit_email_text);

        adapter = new EmailAdapter(this, mEmails);
        setListAdapter(adapter);

        mSubmitButton = (Button) view.findViewById(R.id.submit_email);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });
    }

    private void sendEmail() {
        JSONObject email = constructEmail();

        if (email != null) {

            mSubmitButton.setEnabled(false);

            EmailStore.sendEmail(email, "?return_detail=1", new ResponseCallback(getHandler()) {

                @Override
                public void onSuccess(JSONObject response) {
                    AppSession.showToast("Message sent.");
                }

                @Override
                public void onFailure(JSONObject response) {
                    AppSession.showToast("Could not send email.");
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
                }

                @Override
                public void executeWithHandler(JSONObject response) {
                    mContentText.setText(null);
                    mSubmitButton.setEnabled(true);

                    try {
                        Email responseEmail = EmailStore.fromJSON(response.getJSONObject("data"));
                        if (responseEmail != null) {
                            responseEmail.setSender(mMe);
                            mEmails.add(responseEmail);
                            adapter.changeDataSource(mEmails);
                            getListView().setSelection(adapter.getCount() - 1);
                        }
                    } catch (JSONException e) {
                        //something went wrong
                    }
                }
            });
        }

    }

    private JSONObject constructEmail() {

        String parentId = mParentEmail.getId();
        if (parentId == null) {
            showDataErrorMessage();
            return null;
        }

        String content;

        try {
            content = mContentText.getText().toString();

            if (content == null || content.length() == 0) {
                AppSession.showToast("Email content cannot be blank.");
                return null;
            }
        } catch (NullPointerException e) {
            return null;
        }

        ArrayList<Email.Address> receivers = getReceivers(mParentEmail);
        if (receivers == null || receivers.size() == 0) {
            showDataErrorMessage();
            return null;
        }

        Email.Address sender;

        try {
            sender = Email.getAddressFromUser(mMe);
        } catch (NullPointerException e) {
            return null;
        }

        HashMap<String, Object> email = new HashMap<String, Object>();
        email.put("parent_id", parentId);
        email.put("content", content);
        email.put("receivers", receivers);
        email.put("sender", sender);
        email.put("type", "normal");

        try {
            Gson gson = GlobalGson.getGson();
            return new JSONObject(gson.toJson(email));
        } catch (JSONException e) {
            return null;
        }
    }

    private void showDataErrorMessage() {
        AppSession.showToast("Error sending message");
    }

    private Email.Address getAddressFromUserIfNotMe(User user) {
        try {
            if (mMe.getId().equals(user.getId())) {
                return null;
            }
            return Email.getAddressFromUser(user);

        } catch (NullPointerException e) {
            return null;
        }
    }

    private ArrayList<Email.Address> getAddressesFromUsersThatAreNotMe(ArrayList<User> users) {
        if (users != null) {
            ArrayList<Email.Address> addresses = new ArrayList<Email.Address>();

            for (User user : users) {
                Email.Address address = getAddressFromUserIfNotMe(user);

                if (address != null) addresses.add(address);
            }

            return addresses;
        }

        return null;
    }

    private ArrayList<Email.Address> getReceivers(Email parentEmail) {

        ArrayList<Email.Address> receivers = new ArrayList<Email.Address>();

        try {
            ArrayList<Email.Address> toAddresses = getAddressesFromUsersThatAreNotMe(parentEmail.getToUsers());
            if (toAddresses != null && toAddresses.size() > 0) {
                receivers.addAll(toAddresses);
            }

            ArrayList<Email.Address> ccAddresses = getAddressesFromUsersThatAreNotMe(parentEmail.getCCUsers());
            if (ccAddresses != null) {
                for (Email.Address address : ccAddresses) {
                    if (address != null) {
                        address.setReceiveType("cc");
                    }
                }
                receivers.addAll(ccAddresses);
            }

            ArrayList<Email.Address> nonMemberAddresses = parentEmail.getNonMemberRecipients();
            if (nonMemberAddresses != null && nonMemberAddresses.size() > 0) {
                receivers.addAll(nonMemberAddresses);
            }

            //don't check parent sender for whether the user is me
            Email.Address parentSender = Email.getAddressFromUser(parentEmail.getSender());
            receivers.add(parentSender);

        } catch (NullPointerException e) {
            return null;
        }

        return receivers;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {}

    private void setUpHeader() {
        TextView holder;

        holder = (TextView) mHeaderView.findViewById(R.id.email_from_text);

        try {
            holder.setMovementMethod(LinkMovementMethod.getInstance());
            holder.setText(getUserSpan(mParentEmail.getSender()));
        } catch (NullPointerException e) {
            holder.setText("");
        }

        holder = (TextView) mHeaderView.findViewById(R.id.email_to_text);

        ArrayList<Email.Address> toEmailAddresses = new ArrayList<Email.Address>();
        ArrayList<Email.Address> ccEmailAddresses = new ArrayList<Email.Address>();
        ArrayList<Email.Address> nonMemberRecipients = mParentEmail.getNonMemberRecipients();
        if (nonMemberRecipients != null && nonMemberRecipients.size() > 0) {
            for (Email.Address address : nonMemberRecipients) {
                if (address != null) {
                    String type = address.getReceiveType();

                    if (type != null && type.equals("cc")) {
                        ccEmailAddresses.add(address);
                    } else {
                        toEmailAddresses.add(address);
                    }
                }
            }
        }

        try {
            holder.setMovementMethod(LinkMovementMethod.getInstance());
            ArrayList<User> toUsers = mParentEmail.getToUsers();
            SpannableStringBuilder builder = new SpannableStringBuilder();
            if (toUsers != null && toUsers.size() > 0) {
                builder.append(getUserList(toUsers));
            }
            if (toEmailAddresses.size() > 0) {
                if (toUsers != null && toUsers.size() > 0) {
                    builder.append(", ");
                }
                builder.append(getEmailList(toEmailAddresses));
            }
            holder.setText(builder);
        } catch (NullPointerException e) {
            holder.setText("");
        }

        holder = (TextView) mHeaderView.findViewById(R.id.email_cc_text);

        try {
            holder.setMovementMethod(LinkMovementMethod.getInstance());
            ArrayList<User> ccUsers = mParentEmail.getCCUsers();
            SpannableStringBuilder builder = new SpannableStringBuilder();
            if (ccUsers != null && ccUsers.size() > 0) {
                builder.append(getUserList(ccUsers));
            }
            if (ccEmailAddresses.size() > 0) {
                if (ccUsers != null && ccUsers.size() > 0) {
                    builder.append(", ");
                }
                builder.append(getEmailList(ccEmailAddresses));
            }

            if (builder.length() == 0) {
                throw new NullPointerException();
            }

            holder.setText(builder);
        } catch (NullPointerException e) {
            holder.setText("");
            mHeaderView.findViewById(R.id.cc_field).setVisibility(View.GONE);
        }

        ImageView userAvatar = (ImageView) mHeaderView.findViewById(R.id.user_avatar);
        TextView nameText = (TextView) mHeaderView.findViewById(R.id.name_text);
        TextView cnNumberText = (TextView) mHeaderView.findViewById(R.id.cn_number_text);
        TextView dateText = (TextView) mHeaderView.findViewById(R.id.date_text);
        TextView subjectText = (TextView) mHeaderView.findViewById(R.id.email_subject_text);
        TextView contentText = (TextView) mHeaderView.findViewById(R.id.email_content_text);
        contentText.setMovementMethod(LinkMovementMethod.getInstance());

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        contentText.setTypeface(typeface);

        final User sender = mParentEmail.getSender();
        ImageLoader imageLoader = MyVolley.getImageLoader();

        try {
            String avatarUrl = sender.getAvatar().getView_url();
            imageLoader.get(avatarUrl, ImageLoader.getImageListener(userAvatar,
                            R.drawable.default_user_icon,
                            R.drawable.default_user_icon));
        } catch (NullPointerException e) {
            // no user or no avatar data
        }

        if (sender != null) {
            userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getBaseActivity().openProfilePage(sender);
                }
            });
        }

        try {
            nameText.setText(mParentEmail.getSender().getDisplayName());
        } catch (NullPointerException e) {
            nameText.setText("");
        }

        try {
            cnNumberText.setText(mParentEmail.getSender().getCNNumber());
        } catch (NullPointerException e) {
            cnNumberText.setText("");
        }

        try {
            dateText.setText(mParentEmail.getDisplayTime());
        } catch (NullPointerException e) {
            dateText.setText("");
        }

        try {
            subjectText.setText(mParentEmail.getSubject());
        } catch (NullPointerException e) {
            subjectText.setText("");
        }

        try {
            CNNumberLinker linker = new CNNumberLinker();
            CharSequence content = linker.linkify(mParentEmail.getContent());
            contentText.setText(content);
        } catch (NullPointerException e) {
            contentText.setText("");
        }
    }

    private NavigationActivity getBaseActivity() {
        return (NavigationActivity) getActivity();
    }

    private SpannableStringBuilder getUserList(ArrayList<User> users) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        if (users.size() > 0) {
            builder.append(getUserSpan(users.get(0)));

            for (int i = 1; i < users.size(); i++) {
                builder.append(", ");
                builder.append(getUserSpan(users.get(i)));
            }
        }

        return builder;
    }

    private SpannableString getUserSpan(final User user) {
        final NavigationActivity activity = (NavigationActivity) getActivity();

        String name = user.getDisplayName();
        final SpannableString span = new SpannableString(name);
        span.setSpan(new InternalURLSpan(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.openProfilePage(user);
            }
        }), 0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return span;
    }

    private SpannableStringBuilder getEmailList(ArrayList<Email.Address> addresses) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        if (addresses.size() > 0) {
            String email = getEmail(addresses.get(0));
            if (email != null) builder.append(email);

            for (int i = 1; i < addresses.size(); i++) {
                email = getEmail(addresses.get(i));
                if (email != null) {
                    builder.append(", ");
                    builder.append(email);
                }
            }
        }

        return builder;
    }

    private String getEmail(Email.Address address) {
        if (address != null) {
            return address.getId();
        }

        return null;
    }
}

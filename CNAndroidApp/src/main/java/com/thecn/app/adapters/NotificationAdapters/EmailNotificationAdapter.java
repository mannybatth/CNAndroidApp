package com.thecn.app.adapters.NotificationAdapters;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.adapters.MyFragmentAdapter;
import com.thecn.app.fragments.MyFragments.MyListFragment;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.Email;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.ConexusStore;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.EmailStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.MyVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EmailNotificationAdapter extends MyFragmentAdapter {

    private ArrayList<Email> mEmails = new ArrayList<Email>();
    private ImageLoader imageLoader = MyVolley.getImageLoader();
    private Typeface mTypeface;

    private final int readColor;
    private final int unreadColor;

    static class ViewHolder {
        ImageView userAvatar;
        TextView nameText;
        TextView dateText;
        TextView contentText;
        RelativeLayout contentLayout;
        Button acceptButton, ignoreButton;
        LinearLayout buttonLayout;
        RelativeLayout acceptIgnoreLayout;
        TextView messageText;

        public void setButtonsEnabled(boolean enabled) {
            if (acceptButton != null) {
                acceptButton.setEnabled(enabled);
            }
            if (ignoreButton != null) {
                ignoreButton.setEnabled(enabled);
            }
            if (contentLayout != null) {
                contentLayout.setClickable(!enabled);
                //absorbs parent's clicks so that cannot go to email page when
                //clickable
            }
        }
    }

    public EmailNotificationAdapter(MyListFragment fragment) {
        super(fragment);
        Resources r = fragment.getResources();
        readColor = r.getColor(R.color.white);
        unreadColor = r.getColor(R.color.unread_notification_color);

        mTypeface = Typeface.createFromAsset(fragment.getActivity().getAssets(), "fonts/Roboto-Light.ttf");
    }

    public void addAll(ArrayList<Email> emails) {
        mEmails.addAll(emails);
        notifyDataSetChanged();
    }

    public void clear() {
        mEmails.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mEmails.size();
    }

    @Override
    public Email getItem(int position) {
        return mEmails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (null == convertView) {
            convertView = getLayoutInflater().inflate(R.layout.email_list_item, parent, false);

            holder = new ViewHolder();

            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.nameText = (TextView) convertView.findViewById(R.id.name_text);
            holder.dateText = (TextView) convertView.findViewById(R.id.date_text);
            holder.contentText = (TextView) convertView.findViewById(R.id.content_text);
            holder.contentText.setTypeface(mTypeface);
            holder.contentLayout = (RelativeLayout) convertView.findViewById(R.id.email_notification_parent_layout);
            holder.acceptIgnoreLayout = (RelativeLayout) convertView.findViewById(R.id.email_list_item_accept_reject);
            holder.buttonLayout = (LinearLayout) convertView.findViewById(R.id.accept_reject_button_layout);
            holder.acceptButton = (Button) convertView.findViewById(R.id.accept_button);
            holder.ignoreButton = (Button) convertView.findViewById(R.id.ignore_button);
            holder.messageText = (TextView) convertView.findViewById(R.id.accept_reject_message);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Email email = getItem(position);
        final User sender = email.getSender();

        holder.userAvatar.setTag(position);

        if (email.isUnread()) {
            holder.contentLayout.setBackgroundColor(unreadColor);
        } else {
            holder.contentLayout.setBackgroundColor(readColor);
        }

        if (sender != null) {
            holder.userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getNavigationActivity().openProfilePage(sender);
                }
            });
        }

        String contentString = null;

        if (email.getType().equals("course_invite")) {
            final Course course = email.getCourse();

            holder.contentText.setMaxLines(Integer.MAX_VALUE);
            holder.contentText.setEllipsize(null);

            String userName;
            try {
                userName = email.getSender().getDisplayName();
            } catch (NullPointerException e) {
                userName = "";
            }

            if (course != null) {

                holder.messageText.setText("");
                holder.acceptIgnoreLayout.setVisibility(View.VISIBLE);

                String courseName;
                try {
                    courseName = course.getName();
                } catch (NullPointerException e) {
                    courseName = "";
                }

                contentString = userName
                        + " has invited you to join the course "
                        + courseName;

                final ViewHolder finalHolder = holder;
                final String courseID = course.getId();
                final String userID = AppSession.getInstance().getUser().getId();
                final String inviteEmailID = email.getId();

                holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            setButtonsEnabled(false, finalHolder);

                            CourseStore.joinCourse(courseID, userID, inviteEmailID, new ResponseCallback(getHandler()) {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    try {
                                        finalHolder.buttonLayout.setVisibility(View.GONE);
                                        finalHolder.messageText.setText("Accepted course invite!");

                                        getNavigationActivity().updateCourses();
                                        sendDeleteRequest(inviteEmailID);
                                    } catch (NullPointerException e) {
                                        setButtonsEnabled(true, finalHolder);
                                        //view no longer in memory
                                    }
                                }

                                @Override
                                public void onFailure(JSONObject response) {
                                    AppSession.showToast("Unable to join course");

                                    setButtonsEnabled(true, finalHolder);
                                }

                                @Override
                                public void onError(Exception error) {
                                    StoreUtil.showExceptionMessage(error);
                                    setButtonsEnabled(true, finalHolder);
                                    //something went wrong
                                }
                            });
                        } catch (NullPointerException e) {
                            setButtonsEnabled(true, finalHolder);
                            //no course data
                        }
                    }
                });

                holder.ignoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finalHolder.buttonLayout.setVisibility(View.GONE);
                        finalHolder.messageText.setText("Ignored course invite!");

                        sendDeleteRequest(inviteEmailID);
                    }
                });
            } else {
                contentString = userName
                        + " has invited you to join a course, "
                        + "but this course has been deleted.";
            }

        } else if (email.getType().equals("conexus_invite")) {
            final Conexus conexus = email.getConexus();

            holder.contentText.setMaxLines(Integer.MAX_VALUE);
            holder.contentText.setEllipsize(null);

            String userName;
            try {
                userName = email.getSender().getDisplayName();
            } catch (NullPointerException e) {
                userName = "";
            }

            if (conexus != null) {

                holder.messageText.setText("");
                holder.acceptIgnoreLayout.setVisibility(View.VISIBLE);

                String conexusName;
                try {
                    conexusName = conexus.getName();
                } catch (NullPointerException e) {
                    conexusName = "";
                }

                contentString = userName
                        + " has invited you to join the conexus "
                        + conexusName;

                final ViewHolder finalHolder = holder;
                final String conexusID = conexus.getId();
                final String userID = AppSession.getInstance().getUser().getId();
                final String inviteEmailID = email.getId();

                holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            setButtonsEnabled(false, finalHolder);

                            ConexusStore.joinConexus(conexusID, userID, inviteEmailID, new ResponseCallback(getHandler()) {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    try {
                                        if (response.getBoolean("result")) {
                                            finalHolder.buttonLayout.setVisibility(View.GONE);
                                            finalHolder.messageText.setText("Accepted conexus invite!");

                                            getNavigationActivity().updateConexuses();
                                            sendDeleteRequest(inviteEmailID);
                                        } else {
                                            AppSession.showToast("Unable to join conexus");
                                            setButtonsEnabled(true, finalHolder);
                                        }
                                    } catch (JSONException e) {
                                        setButtonsEnabled(true, finalHolder);
                                        //assume result not true
                                    } catch (NullPointerException e) {
                                        setButtonsEnabled(true, finalHolder);
                                        //view no longer in memory
                                    }
                                }

                                @Override
                                public void onError(Exception error) {
                                    StoreUtil.showExceptionMessage(error);
                                    setButtonsEnabled(true, finalHolder);
                                    //something went wrong
                                }
                            });
                        } catch (NullPointerException e) {
                            setButtonsEnabled(true, finalHolder);
                            //no conexus data
                        }

                    }
                });

                holder.ignoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finalHolder.buttonLayout.setVisibility(View.GONE);
                        finalHolder.messageText.setText("Ignored conexus invite!");

                        sendDeleteRequest(email.getId());
                    }
                });
            } else {
                contentString = userName
                        + " has invited you to join a conexus, "
                        + "but this conexus has been deleted.";
            }
        } else {
            holder.acceptIgnoreLayout.setVisibility(View.GONE);

            holder.contentText.setMaxLines(3);
            holder.contentText.setEllipsize(TextUtils.TruncateAt.END);
        }

        holder.userAvatar.setImageResource(R.drawable.default_user_icon);

        try {
            String avatarUrl = sender.getAvatar().getView_url();
            imageLoader.get(avatarUrl,
                    MyVolley.getIndexedImageListener(position, holder.userAvatar,
                            R.drawable.default_user_icon,
                            R.drawable.default_user_icon)
            );
        } catch (NullPointerException e) {
            // no user or no avatar data
        }

        try {
            holder.nameText.setText(email.getSender().getDisplayName());
        } catch (NullPointerException e) {
            holder.nameText.setText("");
        }

        try {
            holder.dateText.setText(email.getDisplayTime());
        } catch (NullPointerException e) {
            holder.dateText.setText("");
        }

        try {
            CharSequence content = contentString == null ?
                    Html.fromHtml(email.getContent()) : contentString;
            holder.contentText.setText(content);
        } catch (NullPointerException e) {
            holder.contentText.setText("");
        }

        return convertView;

    }

    private void setButtonsEnabled(boolean enabled, ViewHolder holder) {
        if (holder != null) {
            holder.setButtonsEnabled(enabled);
        }
    }

    private void sendDeleteRequest(String emailID) {

        EmailStore.delete(emailID, new ResponseCallback(getHandler()) {
            @Override
            public void onSuccess(JSONObject response) {
                //don't need to do anything
            }

            @Override
            public void onError(Exception error) {
                //something went wrong
            }
        });
    }

    public void changeDataSource(ArrayList<Email> newEmails) {
        mEmails = newEmails;
    }
}

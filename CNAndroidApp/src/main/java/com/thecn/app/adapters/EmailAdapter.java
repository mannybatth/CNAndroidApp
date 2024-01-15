package com.thecn.app.adapters;

import android.graphics.Typeface;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.R;
import com.thecn.app.fragments.EmailFragment;
import com.thecn.app.models.Email;
import com.thecn.app.models.User.User;
import com.thecn.app.tools.CNNumberLinker;
import com.thecn.app.tools.MyVolley;

import java.util.ArrayList;

public class EmailAdapter extends MyFragmentAdapter {

    private ArrayList<Email> mEmails;
    ImageLoader imageLoader = MyVolley.getImageLoader();
    Typeface mTypeface;

    private CNNumberLinker cnNumberLinker;

    static class ViewHolder {
        ImageView userAvatar;
        TextView nameText;
        TextView dateText;
        TextView contentText;
    }

    public EmailAdapter(EmailFragment fragment, ArrayList<Email> emails) {
        super(fragment);
        mEmails = emails;
        cnNumberLinker = new CNNumberLinker();

        mTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
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

            convertView = getLayoutInflater().inflate(R.layout.email_reply_full, parent, false);

            holder = new ViewHolder();

            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.nameText = (TextView) convertView.findViewById(R.id.name_text);
            holder.dateText = (TextView) convertView.findViewById(R.id.date_text);
            holder.contentText = (TextView) convertView.findViewById(R.id.email_content_text);
            holder.contentText.setMovementMethod(LinkMovementMethod.getInstance());
            holder.contentText.setTypeface(mTypeface);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Email email = getItem(position);
        final User sender = email.getSender();

        holder.userAvatar.setTag(position);

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

        if (sender != null) {
            holder.userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getNavigationActivity().openProfilePage(sender);
                }
            });
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
            CharSequence content = cnNumberLinker.linkify(email.getContent());
            holder.contentText.setText(content);
        } catch (NullPointerException e) {
            holder.contentText.setText("");
        }

        return convertView;

    }

    public void changeDataSource(ArrayList<Email> newEmails) {
        mEmails = newEmails;
        notifyDataSetChanged();
    }
}

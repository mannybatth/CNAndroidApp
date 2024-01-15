package com.thecn.app.adapters.NotificationAdapters;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.R;
import com.thecn.app.adapters.MyFragmentAdapter;
import com.thecn.app.fragments.NotificationFragment;
import com.thecn.app.models.Notification;
import com.thecn.app.models.Notification.NotificationClickCallback;
import com.thecn.app.tools.MyVolley;

import java.util.ArrayList;

public class NotificationAdapter extends MyFragmentAdapter {

    private ArrayList<Notification> mNotifications = new ArrayList<Notification>();
    ImageLoader mImageLoader = MyVolley.getImageLoader();

    private Typeface mTypeface;

    private final int readColor;
    private final int unreadColor;

    static class ViewHolder {
        ImageView userAvatar;
        RelativeLayout contentLayout;
        TextView contentText;
    }

    public NotificationAdapter(NotificationFragment fragment) {
        super(fragment);

        mTypeface = Typeface.createFromAsset(getNavigationActivity().getAssets(), "fonts/Roboto-Light.ttf");

        Resources r = getActivity().getResources();
        readColor = r.getColor(R.color.white);
        unreadColor = r.getColor(R.color.unread_notification_color);
    }

    public void addAll(ArrayList<Notification> notifications) {
        mNotifications.addAll(notifications);
        notifyDataSetChanged();
    }

    public void clear() {
        mNotifications.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mNotifications.size();
    }

    @Override
    public Notification getItem(int position) {
        return mNotifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (null == convertView) {

            convertView = getLayoutInflater().inflate(R.layout.roster_list_item, parent, false);

            holder = new ViewHolder();

            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.contentText = (TextView) convertView.findViewById(R.id.content_text);
            holder.contentLayout = (RelativeLayout) convertView.findViewById(R.id.roster_parent_layout);
            holder.contentText.setTypeface(mTypeface);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Notification notification = getItem(position);
        final NotificationClickCallback callback = notification.getNewCallback(getNavigationActivity());

        holder.userAvatar.setTag(position);

        if (notification.getMark().equals("read")) {
            holder.contentLayout.setBackgroundColor(readColor);
        } else {
            holder.contentLayout.setBackgroundColor(unreadColor);
        }

        holder.userAvatar.setImageResource(R.drawable.default_user_icon);

        String avatarUrl = notification.getAvatarUrl();
        mImageLoader.get(avatarUrl,
                MyVolley.getIndexedImageListener(position, holder.userAvatar,
                        R.drawable.default_user_icon,
                        R.drawable.default_user_icon)
        );

        holder.contentText.setText(notification.getNotificationDescription());

        holder.userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onUserClick();
            }
        });

        return convertView;

    }
}

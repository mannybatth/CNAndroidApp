package com.thecn.app.adapters;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.R;
import com.thecn.app.fragments.MyFragments.MyFragmentInterface;
import com.thecn.app.models.User.User;
import com.thecn.app.tools.MyVolley;

import java.util.ArrayList;

public class RosterAdapter extends MyFragmentAdapter {

    private ArrayList<User> mUsers = new ArrayList<User>();
    private ImageLoader imageLoader = MyVolley.getImageLoader();

    private Typeface mTypeface;

    static class ViewHolder {
        ImageView userAvatar, userFlag;
        TextView userName;
    }

    public RosterAdapter(MyFragmentInterface fragment) {
        super(fragment);

        mTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
    }

    public void clear() {
        mUsers.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<User> users) {
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public User getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final User user = getItem(position);

        ViewHolder holder;

        if (null == convertView) {
            convertView = getLayoutInflater().inflate(R.layout.roster_list_item, parent, false);

            holder = new ViewHolder();

            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.userFlag = (ImageView) convertView.findViewById(R.id.user_flag);
            holder.userName = (TextView) convertView.findViewById(R.id.content_text);
            holder.userName.setTypeface(mTypeface);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.userAvatar.setTag(position);
        holder.userFlag.setTag(position);

        try {
            holder.userAvatar.setImageResource(R.drawable.default_user_icon);
            String avatarUrl = user.getAvatar().getView_url() + ".w160.jpg";
            imageLoader.get(avatarUrl,
                    MyVolley.getIndexedImageListener(position, holder.userAvatar,
                            R.drawable.default_user_icon,
                            R.drawable.default_user_icon)
            );
        } catch (Exception e) {
            //whoops
        }

        try {
            holder.userName.setText(user.getDisplayName());
        } catch (Exception e) {
            //whoops
        }

        try {
            holder.userFlag.setImageResource(0);
            imageLoader.get(user.getCountry().getFlagURL(),
                    MyVolley.getIndexedImageListener(position, holder.userFlag,
                            0,
                            0));
        } catch (NullPointerException e) {
            // no country flag
        }

        return convertView;

    }
}

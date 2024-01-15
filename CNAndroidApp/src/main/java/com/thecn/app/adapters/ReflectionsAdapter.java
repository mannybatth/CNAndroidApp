package com.thecn.app.adapters;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.R;
import com.thecn.app.fragments.PostFragment;
import com.thecn.app.models.Reflection;
import com.thecn.app.tools.MyVolley;

import java.util.ArrayList;

public class ReflectionsAdapter extends MyFragmentAdapter {

    private static final String TAG = ReflectionsAdapter.class.getSimpleName();

    private ArrayList<Reflection> mReflections = new ArrayList<Reflection>();
    ImageLoader imageLoader = MyVolley.getImageLoader();

    static class ViewHolder {
        TextView reflectionContentTextView;
        TextView usernameTextView;
        TextView reflectionTimeTextView;
        ImageView userAvatar;
    }

    public ReflectionsAdapter(PostFragment fragment) {
        super(fragment);
    }

    public void add(Reflection reflection) {
        mReflections.add(0, reflection);
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<Reflection> reflections) {
        mReflections.addAll(reflections);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mReflections.size();
    }

    @Override
    public Reflection getItem(int position) {
        return mReflections.get(mReflections.size() - position - 1);
    }

    @Override
    public long getItemId(int position) {
        return mReflections.size() - position - 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Reflection theReflection = getItem(position);

        ViewHolder holder;

        if (null == convertView) {
            convertView = getLayoutInflater().inflate(R.layout.reflection_view, parent, false);
            holder = new ViewHolder();

            holder.reflectionContentTextView = (TextView) convertView.findViewById(R.id.reflection_content);
            holder.reflectionContentTextView.setMovementMethod(LinkMovementMethod.getInstance());
            holder.reflectionTimeTextView = (TextView) convertView.findViewById(R.id.reflection_time);
            holder.usernameTextView = (TextView) convertView.findViewById(R.id.content_text);
            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_icon);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.userAvatar.setTag(position);

        CharSequence text = theReflection.getProcessedText();
        holder.reflectionContentTextView.setText(text);

        holder.usernameTextView.setText(theReflection.getUser().getDisplayName());
        holder.reflectionTimeTextView.setText(theReflection.getDisplayTime());

        String avatarUrl = theReflection.getUser().getAvatar().getView_url() + ".w160.jpg";

        imageLoader.get(avatarUrl,
                MyVolley.getIndexedImageListener(position, holder.userAvatar,
                        R.drawable.default_user_icon,
                        R.drawable.default_user_icon));

        holder.userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNavigationActivity().openProfilePage(theReflection.getUser());
            }
        });

        return convertView;

    }

    public void changeDataSource(ArrayList<Reflection> newReflectionsList) {
        this.mReflections = newReflectionsList;
    }

}

package com.thecn.app.adapters;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.thecn.app.AppSession;
import com.thecn.app.R;

import java.util.ArrayList;

/**
 * Created by pheebner on 1/24/14.
 */
public class VideoLinkAdapter extends BaseAdapter {
    public static final String TAG = VideoLinkAdapter.class.getSimpleName();

    private ArrayList<String> mLinks;
    private Button removeAllVideosButton;
    private EditText linkEditText;
    private Fragment mFragment;

    private static final String linkRegex = "(https?://)?(www\\.)?" +
            "(youtube(-nocookie)?\\.com/(((e(mbed)?|v|user)/.*?)|((watch)?(\\?feature=player_embedded)?[\\?&]v=))" +
            "|(youtu\\.be/))" +
            "[A-Za-z0-9-_]{11}.*";

    public VideoLinkAdapter(Fragment fragment) {
        super();
        mFragment = fragment;
        mLinks = new ArrayList<String>();
    }

    static class ViewHolder {
        TextView linkText;
        ImageButton removeButton;
    }

    @Override
    public String getItem(int position) {
        return mLinks.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public String[] getAllItems() {
        String[] links = new String[mLinks.size()];
        return mLinks.toArray(links);
    }

    @Override
    public int getCount() {
        return mLinks.size();
    }

    public void add(String link) {
        if (link.matches(linkRegex)) {
            mLinks.add(link);
            setButtonEnabled();
            notifyDataSetChanged();
            linkEditText.setText("");
        }
        else
            AppSession.showToast("Not a valid YouTube link.");
    }

    public void remove(int position) {
        mLinks.remove(position);
        setButtonEnabled();
        notifyDataSetChanged();
    }

    public void removeAll() {
        mLinks.clear();
        setButtonEnabled();
        notifyDataSetChanged();
    }

    public void setRemoveAllVideosButton(Button button) {
        removeAllVideosButton = button;
    }

    public void setLinkEditText(EditText text) {
        linkEditText = text;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = mFragment.getActivity().getLayoutInflater();

            convertView = inflater.inflate(R.layout.youtube_link_view, parent, false);

            holder = new ViewHolder();
            holder.linkText = (TextView) convertView.findViewById(R.id.link_text);
            holder.removeButton = (ImageButton) convertView.findViewById(R.id.remove_link);

            convertView.setTag(holder);
        } else holder = (ViewHolder) convertView.getTag();

        holder.linkText.setText(mLinks.get(position));

        final int pos = position;
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(pos);
            }
        });

        return convertView;
    }

    public void setButtonEnabled() {
        removeAllVideosButton.setEnabled(getCount() > 0);
    }
}

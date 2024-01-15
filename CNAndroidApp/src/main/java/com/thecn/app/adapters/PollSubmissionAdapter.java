package com.thecn.app.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.models.PollItem;
import com.thecn.app.tools.MyVolley;

import java.util.ArrayList;

/**
 * Created by Philjay on 7/16/2014.
 */
public class PollSubmissionAdapter extends MyFragmentAdapter {

    private ArrayList<PollItem.Submission> mSubmissions = new ArrayList<PollItem.Submission>();

    private interface GetViewMethod {
        public View getView(int position, View convertView, ViewGroup parent);
    }

    private GetViewMethod mGetViewMethod;

    public PollSubmissionAdapter(MyFragment fragment, PollItem.SubmissionDisplayType displayType) {
        super(fragment);

        switch (displayType) {
            case USER_ANSWER:
                mGetViewMethod = new GetViewMethod() {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return getAllView(position, convertView, parent);
                    }
                };
                break;
            case ANSWER:
                mGetViewMethod = new GetViewMethod() {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return getAnswerOnlyView(position, convertView, parent);
                    }
                };
                break;
            default:
                throw new IllegalStateException("DisplayType must be USER_ANSWER or ANSWER");
        }
    }

    public void clear() {
        mSubmissions.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<PollItem.Submission> responses) {
        mSubmissions.addAll(responses);
        notifyDataSetChanged();
    }

    public int getCount() {
        return mSubmissions.size();
    }

    public PollItem.Submission getItem(int position) {
        return mSubmissions.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder {
        ImageView userAvatar;
        TextView userName, response;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mGetViewMethod.getView(position, convertView, parent);
    }

    private View getAllView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.poll_response_item, parent, false);

            holder = new ViewHolder();

            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.userName = (TextView) convertView.findViewById(R.id.user_name);
            holder.response = (TextView) convertView.findViewById(R.id.response);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        loadImage(holder.userAvatar, position);
        setUserName(holder.userName, position);

        try {
            holder.response.setText(getItem(position).getAnswers().get(0));
        } catch (Exception e) {
            //data not present
        }

        return convertView;
    }

    public View getAnswerOnlyView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.poll_response_item_no_user, parent, false);
        }

        TextView responseText = (TextView) convertView;
        try {
            responseText.setText(getItem(position).getAnswers().get(0));
        } catch (Exception e) {
            //data not present
        }

        return convertView;
    }

    private void setUserName(TextView textView, int position) {
        try {
            textView.setText(getItem(position).getUser().getDisplayName());
        } catch (Exception e) {
            //data not present
        }
    }

    private void loadImage(ImageView imageView, int position) {
        try {
            PollItem.Submission submission = getItem(position);
            String avatarUrl = submission.getUser().getAvatar().getView_url() + ".w160.jpg";
            MyVolley.loadImage(avatarUrl, imageView, position, R.drawable.default_user_icon);
        } catch (Exception e) {
            //data not there
        }
    }
}

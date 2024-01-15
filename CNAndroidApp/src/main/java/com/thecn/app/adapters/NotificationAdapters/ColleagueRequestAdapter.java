package com.thecn.app.adapters.NotificationAdapters;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.R;
import com.thecn.app.adapters.MyFragmentAdapter;
import com.thecn.app.fragments.ColleagueRequestFragment;
import com.thecn.app.models.ColleagueRequest;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.MyVolley;

import org.json.JSONObject;

import java.util.ArrayList;

public class ColleagueRequestAdapter extends MyFragmentAdapter {

    private ArrayList<ColleagueRequest> mRequests = new ArrayList<ColleagueRequest>();
    ImageLoader imageLoader = MyVolley.getImageLoader();
    ColleagueRequestFragment mFragment;

    static class ViewHolder{
        ImageView userAvatar;
        TextView nameText, dateText, messageText;
        Button acceptButton, rejectButton;
        LinearLayout buttonLayout;
        RelativeLayout contentLayout;
    }

    public ColleagueRequestAdapter(ColleagueRequestFragment fragment) {
        super(fragment);
        mFragment = fragment;
    }

    public void addAll(ArrayList<ColleagueRequest> requests) {
        mRequests.addAll(requests);
    }

    public void clear() {
        mRequests.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mRequests.size();
    }

    @Override
    public ColleagueRequest getItem(int position) {
        return mRequests.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {

            convertView = getLayoutInflater().inflate(R.layout.colleague_req_item, parent, false);

            holder = new ViewHolder();

            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.nameText = (TextView) convertView.findViewById(R.id.name_text);
            holder.dateText = (TextView) convertView.findViewById(R.id.date_text);
            holder.messageText = (TextView) convertView.findViewById(R.id.accept_reject_message);
            holder.acceptButton = (Button) convertView.findViewById(R.id.accept_button);
            holder.rejectButton = (Button) convertView.findViewById(R.id.reject_button);
            holder.buttonLayout = (LinearLayout) convertView.findViewById(R.id.accept_reject_button_layout);
            holder.contentLayout = (RelativeLayout) convertView.findViewById(R.id.c_request_parent_layout);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ColleagueRequest request = getItem(position);

        holder.userAvatar.setTag(position);

        if (!request.isActionTaken()) {
            holder.buttonLayout.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.INVISIBLE);

            final ColleagueRequest.Callback callback = request.getCallback(new ResponseCallback(getHandler()) {
                @Override
                public void onSuccess(JSONObject response) {

                    try {
                        setLayoutForAction(true, holder);

                        request.setAccepted(true);
                        sendRequestActionBroadcast(request);
                    } catch (NullPointerException e) {
                        //view no longer exists
                    }
                }

                @Override
                public void onFailure(JSONObject response) {
                    StoreUtil.showFirstResponseError(response);
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
                    //Something went wrong
                }
            });

            holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onAccept();
                }
            });

            holder.rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onReject();
                }
            });
        } else {
            setLayoutForAction(request.isAccepted(), holder);
        }

        holder.userAvatar.setImageResource(R.drawable.default_user_icon);

        try {
            String avatarUrl = request.getUser().getAvatar().getView_url();
            imageLoader.get(avatarUrl,
                    MyVolley.getIndexedImageListener(position, holder.userAvatar,
                            R.drawable.default_user_icon,
                            R.drawable.default_user_icon)
            );
        } catch (NullPointerException e) {
            // Avatar url data not there
        }

        try {
            holder.dateText.setText(request.getDisplayTime());
        } catch (NullPointerException e) {
            holder.dateText.setText("");
        }

        try {
            holder.nameText.setText(request.getUser().getDisplayName());
        } catch (NullPointerException e) {
            holder.nameText.setText("");
        }

        final User user = request.getUser();
        if (user != null && user.getId() != null) {
            holder.userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getNavigationActivity().openProfilePage(user);
                }
            });
        }

        return convertView;

    }

    private void sendRequestActionBroadcast(ColleagueRequest request) {
        try {
            User user = request.getUser();

            String userId = user.getId();
            User.Relations relations = user.getRelations();

            relations.setPendingColleague(false);
            relations.setColleague(request.isAccepted());

            Intent intent = new Intent("colleague_status");
            intent.putExtra("id", userId);
            intent.putExtra("relations", relations);

            LocalBroadcastManager.getInstance(getNavigationActivity())
                    .sendBroadcast(intent);
        } catch (NullPointerException e) {
            // data not there...
        }
    }

    private void setLayoutForAction(boolean accepted, ViewHolder holder) {
        holder.buttonLayout.setVisibility(View.INVISIBLE);
        holder.messageText.setVisibility(View.VISIBLE);

        String messageText = accepted ? "You are now colleagues!" : "Request rejected!";

        holder.messageText.setText(messageText);
    }
}

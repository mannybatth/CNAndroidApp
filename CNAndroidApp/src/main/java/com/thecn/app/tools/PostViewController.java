package com.thecn.app.tools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.NavigationActivity;
import com.thecn.app.fragments.MyFragments.MyListFragment;
import com.thecn.app.models.Picture;
import com.thecn.app.models.Post;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;

import org.json.JSONObject;

import java.util.ArrayList;

public class PostViewController {

    private static ImageLoader imageLoader = MyVolley.getImageLoader();

    private Post mPost;

    private boolean isFullView;

    private View mRootView;
    private MyListFragment mFragment;

    private TextView postTitleTextView;
    private TextView postContentTextView;

    private TextView usernameTextView;
    private TextView cnNumberTextView;
    private TextView postTimeTextView;
    private TextView postFromTextView;
    private TextView interactNumTextView;
    private TextView userPositionTextView;

    private ImageButton likeButton;
    private ImageButton reflectButton;
    private ImageButton moreOptionsBtn;

    private ImageView userAvatar;
    private ImageView userFlag;

    private HorizontalScrollView picturesScrollView;
    private LinearLayout picturesScrollViewLayout;

    private int userIconHeight, userIconWidth, flagIconHeight, flagIconWidth, thumbSize;

    public PostViewController(MyListFragment fragment) {
        View rootView = fragment.getActivity().getLayoutInflater().inflate(R.layout.post_view, null, false);

        init(fragment, rootView);
    }

    public PostViewController(MyListFragment fragment, View rootView) {
        init(fragment, rootView);
    }

    private void init(MyListFragment fragment, View rootView) {
        mFragment = fragment;
        mRootView = rootView;

        Typeface typeface = Typeface.createFromAsset(getBaseActivity().getAssets(), "fonts/Roboto-Light.ttf");

        isFullView = false;

        postTitleTextView = (TextView) mRootView.findViewById(R.id.post_title);
        postContentTextView = (TextView) mRootView.findViewById(R.id.post_content);

        usernameTextView = (TextView) mRootView.findViewById(R.id.content_text);
        usernameTextView.setTypeface(typeface);
        cnNumberTextView = (TextView) mRootView.findViewById(R.id.cn_number);
        postTimeTextView = (TextView) mRootView.findViewById(R.id.post_time);
        postFromTextView = (TextView) mRootView.findViewById(R.id.post_from_field);
        interactNumTextView = (TextView) mRootView.findViewById(R.id.interact_num_text);
        userPositionTextView = (TextView) mRootView.findViewById(R.id.user_position_text);

        userAvatar = (ImageView) mRootView.findViewById(R.id.user_avatar);
        userFlag = (ImageView) mRootView.findViewById(R.id.user_flag);

        likeButton = (ImageButton) mRootView.findViewById(R.id.like_operate_btn);
        reflectButton = (ImageButton) mRootView.findViewById(R.id.reflect_operate_btn);
        moreOptionsBtn = (ImageButton) mRootView.findViewById(R.id.more_options_btn);

        picturesScrollView = (HorizontalScrollView) mRootView.findViewById(R.id.picturesScrollView);
        picturesScrollViewLayout = (LinearLayout) mRootView.findViewById(R.id.picturesScrollViewLayout);

        Resources r = getBaseActivity().getResources();

        userIconHeight = (int) r.getDimension(R.dimen.user_icon_height);
        userIconWidth = (int) r.getDimension(R.dimen.user_icon_width);
        flagIconHeight = (int) r.getDimension(R.dimen.user_flag_height);
        flagIconWidth = (int) r.getDimension(R.dimen.user_flag_width);
        thumbSize = (int) r.getDimension(R.dimen.post_view_pictures_height);
    }

    private MyListFragment getFragment() {
        return mFragment;
    }

    private NavigationActivity getBaseActivity() {
        return (NavigationActivity) mFragment.getActivity();
    }

    private PausingHandler getHandler() {
        return mFragment.getHandler();
    }

    public void setPost(Post post) {
        mPost = post;
    }

    public void setFullView(boolean isFullView) {
        this.isFullView = isFullView;
    }

    public View getRootView() {
        return mRootView;
    }

    public void setUpView(Post post) {
        setUpView(post, 0);
    }

    public void setUpView(Post post, int index) {
        mPost = post;

        userAvatar.setTag(index);
        userFlag.setTag(index);

        adaptViewForPost();

        try {
            usernameTextView.setText(mPost.getUser().getDisplayName());
        } catch (NullPointerException e) {
            usernameTextView.setText("");
        }

        try {
            postTimeTextView.setText(mPost.getTimeText());
        } catch (NullPointerException e) {
            postTimeTextView.setText("");
        }

        try {
            cnNumberTextView.setText(mPost.getUser().getCNNumber());
        } catch (NullPointerException e) {
            cnNumberTextView.setText("");
        }

        postFromTextView.setText(mPost.getPostFromText());

        setInteractNumText();

        boolean hideUserPosition;
        try {
            String userPosition = mPost.getUserPosition();

            hideUserPosition = userPosition.length() == 0;
            if (!hideUserPosition) {
                userPositionTextView.setText(userPosition.toUpperCase());

                if (userPosition.equalsIgnoreCase("instructor")) {
                    userPositionTextView.setBackgroundResource(R.drawable.user_position_instructor_display);
                } else if (userPosition.equalsIgnoreCase("cn admin")) {
                    userPositionTextView.setBackgroundResource(R.drawable.user_position_admin_display);
                } else {
                    userPositionTextView.setBackgroundResource(R.drawable.user_position_display);
                }
            }
        } catch (NullPointerException e) {
            hideUserPosition = true;
        }
        if (hideUserPosition) {
            userPositionTextView.setVisibility(View.GONE);
        } else {
            userPositionTextView.setVisibility(View.VISIBLE);
        }

        likeButton.setSelected(mPost.isLiked());
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLikeButtonClick();
            }
        });

        reflectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReflectButtonClicked(mPost);
            }
        });

        moreOptionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMoreOptionsBtnClicked(mPost);
            }
        });

        try {
            String avatarUrl = mPost.getUser().getAvatar().getView_url() + ".w160.jpg";

            userAvatar.setImageResource(R.drawable.default_user_icon);
            imageLoader.get(avatarUrl,
                    MyVolley.getIndexedImageListener(index, userAvatar,
                            R.drawable.default_user_icon,
                            R.drawable.default_user_icon), userIconWidth, userIconHeight
            );
        } catch (NullPointerException e) {
            userAvatar.setImageResource(R.drawable.default_user_icon);
        }

        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBaseActivity().openProfilePage(mPost.getUser());
            }
        });

        try {
            userFlag.setImageResource(0);
            imageLoader.get(mPost.getUser().getCountry().getFlagURL(),
                    MyVolley.getIndexedImageListener(index, userFlag,
                            0,
                            0), flagIconWidth, flagIconHeight);
        } catch (NullPointerException e) {
            // no country flag
        }

        setUpPictures();
    }

    public void setInteractNumText() {
        interactNumTextView.setText(mPost.getInteractNumText());
    }

    private void adaptViewForPost() {

        Post.Type type = mPost.getEnumType();
        postContentTextView.setMovementMethod(LinkMovementMethod.getInstance());

        if (type != Post.Type.QUIZ) {
            showTitleIfExists();
        }

        postContentTextView.setText(mPost.getContentText());
    }

    //set height of title to zero instead of GONE, so that content of post will still know where
    //to insert itself if there is no title
    private void showTitleIfExists() {
        Spanned title = mPost.getProcessedTitle();

        if (title != null) {
            postTitleTextView.setText(title);
            setViewHeight(postTitleTextView, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            setViewHeight(postTitleTextView, 0);
        }
    }

    private void setViewHeight(View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
    }

    private void setUpPictures() {
        picturesScrollViewLayout.removeAllViews();

        final ArrayList<Post.Video> postVideos = mPost.getVideos();
        final ArrayList<Picture> postPictures = mPost.getPictures();

        int picIndex = 0;
        boolean displayPictures = (postPictures != null && postPictures.size() > 0)
                || (postVideos != null && postVideos.size() > 0);
        if (displayPictures) {
            picturesScrollView.setVisibility(View.VISIBLE);

            for (final Post.Video video : postVideos) {
                View[] thumbViews = makeThumbLayout();
                RelativeLayout layout = (RelativeLayout) thumbViews[0];
                ImageView picView = (ImageView) thumbViews[1];
                picView.setImageResource(R.drawable.blank_image);

                String videoID = video.getVideoID();

                if (videoID != null) {

                    String url = "https://img.youtube.com/vi/";
                    url += videoID + "/default.jpg";
                    imageLoader.get(url,
                            ImageLoader.getImageListener(picView,
                                    0,
                                    0), thumbSize, thumbSize);
                }

                ImageView playBtn = new ImageView(getBaseActivity());
                RelativeLayout.LayoutParams params =
                        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                playBtn.setLayoutParams(params);
                playBtn.setImageResource(R.drawable.post_video_play_btn);

                layout.addView(picView);
                layout.addView(playBtn);

                picView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent webIntent = new Intent(Intent.ACTION_VIEW);
                        webIntent.setData(Uri.parse(video.getViewURL()));
                        getBaseActivity().startActivity(webIntent);
                    }
                });

                picturesScrollViewLayout.addView(layout);
                picturesScrollView.fullScroll(HorizontalScrollView.FOCUS_BACKWARD);
            }

            for (Picture picture : postPictures) {
                View[] thumbViews = makeThumbLayout();
                RelativeLayout layout = (RelativeLayout) thumbViews[0];
                ImageView picView = (ImageView) thumbViews[1];

                imageLoader.get(picture.getPictureURL() + ".w320.jpg",
                        ImageLoader.getImageListener(picView,
                                0,
                                0), thumbSize, thumbSize);
                layout.addView(picView);

                final int i = picIndex;
                picView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onPictureAttachmentClick(postPictures, i);
                    }
                });

                picturesScrollViewLayout.addView(layout);
                picturesScrollView.fullScroll(HorizontalScrollView.FOCUS_BACKWARD);
                picIndex++;
            }
        } else {
            picturesScrollView.setVisibility(View.GONE);
        }
    }

    //returns layout and imageview
    private View[] makeThumbLayout() {

        RelativeLayout layout = new RelativeLayout(getBaseActivity());
        layout.setLayoutParams(new RelativeLayout.LayoutParams(thumbSize, thumbSize));

        ImageView picView = new ImageView(getBaseActivity());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(thumbSize, thumbSize);
        params.setMargins(0, 0, 10, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        picView.setLayoutParams(params);
        picView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        return new View[]{layout, picView};
    }

    public void onLikeButtonClick() {
        likeButton.setEnabled(false);
        if (mPost.isLiked()) {
            setPostLiked(false);
            updatePostViewLikes();

            ResponseCallback callback = new ResponseCallback() {
                @Override
                public void onFailure(JSONObject response) {
                    AppSession.showToast("Could not unlike post...");
                    setPostLiked(true);
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
                    setPostLiked(true);
                }

                @Override
                public void executeWithHandler(JSONObject response) {
                    ExecutionType type = getExecutionType();
                    if (type == ExecutionType.FAILURE || type == ExecutionType.ERROR) {
                        updatePostViewLikes();
                    }

                    likeButton.setEnabled(true);
                }
            };
            callback.setHandler(getHandler(), ResponseCallback.HANDLE_NONE);

            PostStore.unlikePost(mPost, callback);
        } else {
            setPostLiked(true);
            updatePostViewLikes();

            ResponseCallback callback = new ResponseCallback() {
                @Override
                public void onFailure(JSONObject response) {
                    AppSession.showToast("Could not like post...");
                    setPostLiked(false);
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
                    setPostLiked(false);
                }

                @Override
                public void executeWithHandler(JSONObject response) {
                    ExecutionType type = getExecutionType();
                    if (type == ExecutionType.FAILURE || type == ExecutionType.ERROR) {
                        updatePostViewLikes();
                    }

                    likeButton.setEnabled(true);
                }
            };
            callback.setHandler(getHandler(), ResponseCallback.HANDLE_NONE);

            PostStore.likePost(mPost, callback);
        }
    }

    private void setPostLiked(boolean liked) {
        try {
            mPost.setLiked(liked);

            int numLikes = mPost.getCount().getLikes();
            if (liked) numLikes++;
            else numLikes--;
            mPost.getCount().setLikes(numLikes);
            PostChangeHandler.sendUpdatedBroadcast(mPost);
        } catch (NullPointerException e) {
            //data not there...
        }
    }

    private void updatePostViewLikes() {
        try {
            boolean liked = mPost.isLiked();

            likeButton.setSelected(liked);
            interactNumTextView.setText(mPost.getInteractNumText());

        } catch (NullPointerException e) {
            // data not there...
        }
    }

    public void onReflectButtonClicked(Post post) {
        getBaseActivity().openPostPage(post, true);
    }

    public void onMoreOptionsBtnClicked(final Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseActivity());
        String[] options;
        if (post.isDeletable()) {
            if (isFullView) {
                options = new String[]{"Show Likes", "Delete Post"};
            } else {
                options = new String[]{"Show Likes", "Show Reflections", "Delete Post"};
            }
        } else {
            if (isFullView) {
                options = new String[]{"Show Likes"};
            } else {
                options = new String[]{"Show Likes", "Show Reflections"};
            }
        }
        builder.setTitle(null)
                .setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                getBaseActivity().openPostLikesActivity(post);
                                break;
                            case 1:
                                if (isFullView && post.isDeletable())
                                    deletePost(post);
                                else
                                    getBaseActivity().openPostPage(post, false);
                                break;
                            case 2:
                                deletePost(post);
                                break;
                            default:
                                break;
                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void onPictureAttachmentClick(ArrayList<Picture> pics, int currentIndex) {
        if (mAllowInteraction) {
            getBaseActivity().openPhotoGalleryViewerActivity(pics, currentIndex);
        }
    }

    private boolean mAllowInteraction = true;

    public void allowInteraction(boolean allow) {
        mAllowInteraction = allow;

        likeButton.post(new Runnable() {
            @Override
            public void run() {
                likeButton.setEnabled(mAllowInteraction);
            }
        });

        reflectButton.post(new Runnable() {
            @Override
            public void run() {
                reflectButton.setEnabled(mAllowInteraction);
            }
        });

        moreOptionsBtn.post(new Runnable() {
            @Override
            public void run() {
                moreOptionsBtn.setEnabled(mAllowInteraction);
            }
        });
    }

    //this callback is for UI updates when the callbacks are performed
    public interface DeleteCallback {
        public void onRequest();
        public void onConfirm();
        public void onResponse();
    }

    private DeleteCallback mDeleteCallback;

    public void registerDeleteCallback(DeleteCallback callback) {
        mDeleteCallback = callback;
    }

    public void deletePost(final Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseActivity());
        builder.setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (mDeleteCallback != null) {
                            mDeleteCallback.onRequest();
                        }

                        PostStore.deletePost(post.getId(), new ResponseCallback(getHandler()) {
                            @Override
                            public void onSuccess(JSONObject response) {
                                PostChangeHandler.sendDeletedBroadcast(post);
                                if (mDeleteCallback != null) {
                                    mDeleteCallback.onResponse();
                                    mDeleteCallback.onConfirm();
                                }
                            }

                            @Override
                            public void onError(Exception error) {
                                StoreUtil.showExceptionMessage(error);
                                if (mDeleteCallback != null) {
                                    mDeleteCallback.onResponse();
                                }
                            }
                        });

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}

package com.thecn.app.fragments.Profile;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.NavigationActivity;
import com.thecn.app.models.Picture;
import com.thecn.app.models.User.Score;
import com.thecn.app.models.User.User;
import com.thecn.app.models.User.UserProfile;
import com.thecn.app.services.UpdateService;
import com.thecn.app.stores.ColleagueRequestStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.MyVolley;
import com.thecn.app.tools.PausingHandler;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by philjay on 4/10/14.
 */
public class ProfileHeader {

    private View mView; //root view of header
    private User mUser;
    private final Object userLock = new Object();

    private ImageButton mColleagueButton;
    private final Object colleagueButtonLock = new Object();
    private ImageButton mFollowButton;
    private final Object followButtonLock = new Object();

    private volatile long mColleagueLastLocalChangeTime = 0;
    private volatile long mFollowLastLocalChangeTime = 0;

    private ProfilePostsFragment mFragment;

    private UpdateService mService;
    private UpdateService.Updater mUpdater;

    private AlertDialog mAlertDialog;

    private ImageLoader imageLoader = MyVolley.getImageLoader();

    //used to change buttons appropriately if the user accepts or rejects a colleague request
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String userId = intent.getStringExtra("id");
            synchronized (userLock) {
                boolean isThisUser = userId != null && mUser != null
                        && userId.equals(mUser.getId());

                if (isThisUser) {
                    User.Relations relations = (User.Relations) intent.getSerializableExtra("relations");

                    if (relations != null) {
                        setColleagueState(relations);
                    }
                }
            }
        }
    };

    /**
     * DO NOT use the same instance across Activity recreation
     * @param view header view
     * @param user profile user
     * @param fragment activity view is attached to
     */
    public ProfileHeader(View view, User user, ProfilePostsFragment fragment) {
        mView = view;
        mUser = user;
        mFragment = fragment;
    }

    private NavigationActivity getBaseActivity() {
        return (NavigationActivity) mFragment.getActivity();
    }

    private PausingHandler getHandler() {
        return mFragment.getHandler();
    }

    public void setUpHeader() {

        String avatarUrl = mUser.getAvatar().getView_url() + ".w160.jpg";

        ImageView avatarImgView = (ImageView) mView.findViewById(R.id.avatarImg);
        imageLoader.get(avatarUrl,
                ImageLoader.getImageListener(avatarImgView,
                        R.drawable.default_user_icon,
                        R.drawable.default_user_icon));

        avatarImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Picture pic = new Picture();
                pic.setPictureURL(mUser.getAvatar().getView_url());
                ArrayList<Picture> picsArr = new ArrayList<Picture>();
                picsArr.add(pic);
                getBaseActivity().openPhotoGalleryViewerActivity(picsArr, 0);
            }
        });

        TextView userNameTxtView = (TextView) mView.findViewById(R.id.userName);
        Typeface typeface = Typeface.createFromAsset(getBaseActivity().getAssets(), "fonts/Roboto-Light.ttf");
        userNameTxtView.setTypeface(typeface);
        userNameTxtView.setText(mUser.getDisplayName());

        String cnNumber = mUser.getCNNumber();
        cnNumber = cnNumber != null ? cnNumber : "";
        TextView cnNumTxtView = (TextView) mView.findViewById(R.id.cn_number);
        cnNumTxtView.setTypeface(typeface);
        cnNumTxtView.setText(cnNumber);

        String userAbout, positionContent;

        UserProfile profile = mUser.getUserProfile();

        try {
            positionContent = profile.getUserPosition().getUserPositionString();
        } catch (NullPointerException e) {
            positionContent = "";
        }

        try {
            userAbout = profile.getAbout();
        } catch (NullPointerException e) {
            userAbout = "";
        }

        TextView userPositionTxt = (TextView) mView.findViewById(R.id.header_user_position);
        userPositionTxt.setTypeface(typeface);
        if (positionContent != null && positionContent.length() > 0) {
            userPositionTxt.setVisibility(View.GONE);
        } else {
            userPositionTxt.setText(positionContent);
        }

        TextView headerAbout = (TextView) mView.findViewById(R.id.header_about);
        headerAbout.setTypeface(typeface);
        if (userAbout != null && userAbout.length() > 0) {
            headerAbout.setVisibility(View.GONE);
        } else {
            headerAbout.setText(userAbout);
        }

        setUserScore();

        LocalBroadcastManager.getInstance(getBaseActivity())
                .registerReceiver(mReceiver, new IntentFilter("colleague_status"));

        User.Relations relations = mUser.getRelations();

        if (relations != null) {
            if (relations.isMyself()) {
                //can't follow or colleague request yourself
                mView.findViewById(R.id.interaction_pane).setVisibility(View.GONE);
            } else {
                initFollowButton();
                initColleagueButton();
                initUpdater();
            }
        } else {
            mView.findViewById(R.id.interaction_pane).setVisibility(View.GONE);
        }
    }

    public void onDestroyView() {
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
    }

    private void initFollowButton() {
        mFollowButton = (ImageButton) mView.findViewById(R.id.follow_button);
        setFollowButtonDrawable(isFollowing());

        mFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFollowButtonEnabled(false);

                if (isFollowing()) {
                    //set change immediately, but change back if there was an error
                    setFollowingState(false);

                    synchronized (userLock) {
                        UserStore.stopFollowingUser(mUser.getId(), new ResponseCallback(getHandler()) {
                            @Override
                            public void onFailure(JSONObject response) {
                                setFollowingState(true);
                            }

                            @Override
                            public void onError(Exception error) {
                                setFollowingState(true);
                            }

                            @Override
                            public void onPostExecute() {
                                setFollowButtonEnabled(true);
                            }
                        });
                    }
                } else {
                    //set change immediately, but change back if there was an error
                    setFollowingState(true);

                    synchronized (userLock) {
                        UserStore.followUser(mUser.getId(), new ResponseCallback(getHandler()) {
                            @Override
                            public void onFailure(JSONObject response) {
                                setFollowingState(false);
                            }

                            @Override
                            public void onError(Exception error) {
                                setFollowingState(false);
                            }

                            @Override
                            public void onPostExecute() {
                                setFollowButtonEnabled(true);
                            }
                        });
                    }
                }
            }
        });
    }

    private void initColleagueButton() {
        mColleagueButton = (ImageButton) mView.findViewById(R.id.colleague_button);
        setColleagueButtonDrawable(readColleagueState());

        mColleagueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColleagueButtonEnabled(false);
                final ColleagueRequestState state = readColleagueState();

                switch (state) {
                    case NO_REQUEST:
                        colleagueButtonOnClickNoRequest();
                        break;
                    case PENDING:
                        colleagueButtonOnClickPending();
                        break;
                    case COLLEAGUES:
                        colleagueButtonOnClickColleagues();
                        break;
                }
            }
        });
    }

    private void colleagueButtonOnClickNoRequest() {
        //set change immediately, but change back if there was an error
        setColleagueState(ColleagueRequestState.PENDING);

        ColleagueRequestStore.sendRequest(mUser.getId(), new ColleagueButtonCallback(getHandler()) {
            @Override
            void resetStatus() {
                setColleagueState(ColleagueRequestState.NO_REQUEST);
            }
        });
    }

    private void colleagueButtonOnClickPending() {
        Boolean notMyRequest = null;
        synchronized (userLock) {
            User.Relations relations = mUser.getRelations();

            if (relations != null) {
                notMyRequest = relations.isPassiveColleague();
            }
        }

        if (notMyRequest != null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getBaseActivity());
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    setColleagueButtonEnabled(true);
                }
            });

            if (notMyRequest) {
                pendingColleagueNotMyRequest(builder);
            } else {
                pendingColleagueMyRequest(builder);
            }

            mAlertDialog = builder.create();
            mAlertDialog.show();
        }
    }

    private void pendingColleagueNotMyRequest(AlertDialog.Builder builder) {
        String userName = null;

        synchronized (userLock) {
            try {
                userName = mUser.getDisplayName();
            } catch (NullPointerException e) {
                //no user data...??
            } finally {
                if (userName == null) userName = "this user";
            }
        }

        builder.setMessage("Would you like to add " + userName + " to your colleagues?")
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set change immediately, but change back if there was an error
                        setColleagueState(ColleagueRequestState.COLLEAGUES);

                        ColleagueRequestStore.sendRequest(mUser.getId(), new ColleagueButtonCallback(getHandler()) {
                            @Override
                            void resetStatus() {
                                setColleagueState(ColleagueRequestState.PENDING);
                            }
                        });
                    }
                })
                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set change immediately, but change back if there was an error
                        setColleagueState(ColleagueRequestState.NO_REQUEST);

                        ColleagueRequestStore.cancelRequest(mUser.getId(), new ColleagueButtonCallback(getHandler()) {
                            @Override
                            void resetStatus() {
                                setColleagueState(ColleagueRequestState.PENDING);
                            }
                        });
                    }
                });
    }

    private void pendingColleagueMyRequest(AlertDialog.Builder builder) {

        builder.setMessage("Are you sure you want to cancel this colleague request?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set change immediately, but change back if there was an error
                        setColleagueState(ColleagueRequestState.NO_REQUEST);

                        ColleagueRequestStore.cancelRequest(mUser.getId(), new ColleagueButtonCallback(getHandler()) {
                            @Override
                            void resetStatus() {
                                setColleagueState(ColleagueRequestState.PENDING);
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setColleagueButtonEnabled(true);
                    }
                });
    }

    private void colleagueButtonOnClickColleagues() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseActivity());

        builder.setMessage("Are you sure you want to remove this colleague?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set change immediately, but change back if there was an error
                        setColleagueState(ColleagueRequestState.NO_REQUEST);

                        ColleagueRequestStore.removeColleague(mUser.getId(), new ColleagueButtonCallback(getHandler()) {
                            @Override
                            void resetStatus() {
                                setColleagueState(ColleagueRequestState.COLLEAGUES);
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setColleagueButtonEnabled(true);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        setColleagueButtonEnabled(true);
                    }
                });

        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private void initUpdater() {
        mService = getBaseActivity().getUpdateService();
        if (mService != null) {
            mUpdater = new UpdateService.Updater() {

                @Override
                public void update() {
                    synchronized (userLock) {

                        UserStore.getUserById(mUser.getId(),
                                "?with_user_relations=1", new ResponseCallback(getHandler()) {
                            @Override
                            public void onSuccess(JSONObject response) {
                                User user = UserStore.getData(response);

                                if (user != null) {
                                    updateButtons(user.getRelations());
                                }
                            }

                            @Override
                            public void onError(Exception error) {
                                //do nothing
                            }
                        });
                    }
                }
            };

            mService.addUpdater(mUpdater);
        }
    }

    private void updateButtons(User.Relations relations) {
        if (relations != null) {
            long currentTime = System.currentTimeMillis();

            boolean changeColleagueState = currentTime > mColleagueLastLocalChangeTime;

            synchronized (colleagueButtonLock) {
                if (mColleagueButton != null) {
                    changeColleagueState = changeColleagueState && mColleagueButton.isEnabled();
                }
            }

            boolean changeFollowState = currentTime > mFollowLastLocalChangeTime;

            synchronized (followButtonLock) {
                if (mFollowButton != null) {
                    changeFollowState = changeFollowState && mFollowButton.isEnabled();
                }
            }

            if (changeColleagueState) {
                setColleagueState(relations);
            }

            if (changeFollowState) {
                setFollowingState(relations.isFollowing());
            }
        }
    }

    public void removeUpdater() {
        if (mService != null && mUpdater != null) {
            mService.removeUpdater(mUpdater);
        }
    }

    private abstract class ColleagueButtonCallback extends ResponseCallback {
        public ColleagueButtonCallback(PausingHandler handler) {
            super(handler);
        }

        @Override
        public void onFailure(JSONObject response) {
            resetWithError();
        }

        @Override
        public void onError(Exception error) {
            StoreUtil.showExceptionMessage(error);
            resetStatus();
        }

        @Override
        public void onPostExecute() {
            setColleagueButtonEnabled(true);
        }

        private void resetWithError() {
            showErrorMessage();
            resetStatus();
        }

        abstract void resetStatus();
    }

    private void showErrorMessage() {
        AppSession.showToast("Could not add to colleagues due to unexpected error.");
    }

    private enum ColleagueRequestState {
        NO_REQUEST, PENDING, COLLEAGUES
    }

    private ColleagueRequestState readColleagueState() {
        synchronized (userLock) {
            User.Relations relations = mUser.getRelations();

            if (relations != null) {
                if (relations.isColleague()) {
                    return ColleagueRequestState.COLLEAGUES;
                } else if (relations.isPendingColleague()) {
                    return ColleagueRequestState.PENDING;
                }
            }

            return ColleagueRequestState.NO_REQUEST;
        }
    }

    private void setColleagueState(User.Relations relations) {
        if (mUser != null) {
            synchronized (userLock) {
                User.Relations mRelations = mUser.getRelations();

                mRelations.setColleague(relations.isColleague());
                mRelations.setPendingColleague(relations.isPendingColleague());
                mRelations.setPassiveColleague(relations.isPassiveColleague());
            }

            setColleagueButtonDrawable(readColleagueState());
        }
    }

    private void setColleagueState(ColleagueRequestState state) {
        int drawableResource = 0;

        synchronized (userLock) {
            if (mUser != null) {
                User.Relations relations = mUser.getRelations();

                if (relations != null) {
                    switch (state) {
                        case COLLEAGUES:
                            relations.setColleague(true);
                            relations.setPendingColleague(false);
                            drawableResource = R.drawable.colleague_button_image;
                            break;
                        case PENDING:
                            relations.setColleague(false);
                            relations.setPendingColleague(true);
                            drawableResource = R.drawable.pending_button_image;
                            break;
                        case NO_REQUEST:
                            relations.setColleague(false);
                            relations.setPendingColleague(false);
                            drawableResource = R.drawable.not_colleague_button_image;
                            break;
                    }
                }
            }
        }

        setColleagueButtonDrawable(drawableResource);
    }

    private void setColleagueButtonDrawable(int drawableResource) {
        synchronized (colleagueButtonLock) {
            if (mColleagueButton != null) {
                mColleagueButton.setImageResource(drawableResource);
            }
        }
    }

    private void setColleagueButtonDrawable(ColleagueRequestState state) {
        int drawableResource = 0;

        switch (state) {
            case NO_REQUEST:
                drawableResource = R.drawable.not_colleague_button_image;
                break;
            case PENDING:
                drawableResource = R.drawable.pending_button_image;
                break;
            case COLLEAGUES:
                drawableResource = R.drawable.colleague_button_image;
                break;
        }

        setColleagueButtonDrawable(drawableResource);
    }

    private void setColleagueButtonEnabled(boolean enabled) {
        synchronized (colleagueButtonLock) {
            if (mColleagueButton != null) {
                mColleagueButton.setEnabled(enabled);
                mColleagueLastLocalChangeTime = System.currentTimeMillis();
            }
        }
    }

    private boolean isFollowing() {
        synchronized (userLock) {
            if (mUser != null) {
                User.Relations relations = mUser.getRelations();
                if (relations != null) {
                    return relations.isFollowing();
                }
            }
        }

        return false;
    }

    private void setFollowButtonEnabled(boolean enabled) {
        synchronized (followButtonLock) {
            if (mFollowButton != null) {
                mFollowButton.setEnabled(enabled);
                mFollowLastLocalChangeTime = System.currentTimeMillis();
            }
        }
    }

    private void setFollowingState(boolean following) {
        synchronized (userLock) {
            if (mUser != null) {
                mUser.getRelations().setFollowing(following);
            }
        }

        int drawableResource = following ? R.drawable.following_button_image : R.drawable.follow_button_image;

        setFollowButtonDrawable(drawableResource);
    }

    private void setFollowButtonDrawable(int drawableResource) {
        synchronized (followButtonLock) {
            if (mFollowButton != null) {
                mFollowButton.setImageResource(drawableResource);
            }
        }
    }

    private void setFollowButtonDrawable(boolean following) {
        int drawableResource = following ? R.drawable.following_button_image : R.drawable.follow_button_image;

        setFollowButtonDrawable(drawableResource);
    }

    private void setUserScore() {
        Score score = mUser.getScore();
        String scoreText = "";

        if (score != null) {
            scoreText = Integer.toString(score.getTotal());
            scoreText += " Anar Seeds";

            ((TextView) mView.findViewById(R.id.anar_number_text))
                    .setText(scoreText);
        }

        if (scoreText.length() == 0) {
            mView.findViewById(R.id.anar_display_parent)
                    .setVisibility(View.GONE);
        }
    }
}

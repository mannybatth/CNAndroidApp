package com.thecn.app.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.MyActivities.MyFragmentActivity;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.AuthStore;
import com.thecn.app.stores.ConexusStore;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author pheebner
 * @description Login Page.  Reused much of the code by zhenggl
 */
public class LoginActivity extends MyFragmentActivity {

    private Activity loginActivity = this;

    private ImageView appLogoImage;
    private ImageView appLogoImageSmall;

    private EditText usernameField;
    private EditText passwordField;
    private Button loginBtn;
    private TextView registerBtn;

    private ProgressDialog pd;

    private boolean animationCompleted;

    private LoginFragment mLoginFragment;
    private static final String mLoginFragmentTag = "login_fragment";

    //reference for making this part of view visible after logo animation
    private LinearLayout loginLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean wasLoading;

        if (savedInstanceState != null) {
            animationCompleted = savedInstanceState.getBoolean("animation_completed", false);
            wasLoading = savedInstanceState.getBoolean("was_loading", false);

            if (animationCompleted) {
                fadeDuration = translateScaleDuration = fadeViewDuration = 0;
            }

            mLoginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag(mLoginFragmentTag);

        } else {
            AppSession.getInstance().clearSession();
            wasLoading = false;

            mLoginFragment = new LoginFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(mLoginFragment, mLoginFragmentTag)
                    .commit();
        }

        init(wasLoading);
        fadeInLogo();
    }

    private void init(boolean wasLoading) {

        appLogoImageSmall = (ImageView) findViewById(R.id.app_logo_image_small);
        appLogoImageSmall.setVisibility(View.INVISIBLE);
        appLogoImage = (ImageView) findViewById(R.id.app_logo_image);
        appLogoImage.setVisibility(View.INVISIBLE);
        loginLayout = (LinearLayout) findViewById(R.id.login_layout);
        loginLayout.setVisibility(View.INVISIBLE);
        usernameField = (EditText) findViewById(R.id.content_text);
        passwordField = (EditText) findViewById(R.id.pass_edit);
        loginBtn = (Button) findViewById(R.id.login_btn);

        pd = new ProgressDialog(this) {
            @Override
            public void onBackPressed() {
                if (isShowing()) {
                    setLoading(false);
                } else super.onBackPressed();
            }
        };
        pd.setCancelable(false);
        pd.setMessage("Logging In");
        setLoading(wasLoading);

        loginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasFields()) {
                    setLoading(true);
                    login();
                }//end if(hasFields())
            }
        });

        //button links user to course networking home page where they can sign up
        registerBtn = (TextView) findViewById(R.id.register_label);
        registerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                webIntent.setData(Uri.parse("http://www.thecn.com"));
                loginActivity.startActivity(webIntent);
            }
        });
    }

    private void login() {
        String userName = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        mLoginFragment.login(userName, password);
    }

    public static class LoginFragment extends MyFragment {

        private abstract class LoginResponse extends ResponseCallback {
            public LoginResponse() {
                super(LoginFragment.this.getHandler());
            }

            @Override
            public void onFailure(JSONObject response) {
                StoreUtil.showFirstResponseError(response);
                onLoadingDone();
            }

            @Override
            public void onError(Exception error) {
                StoreUtil.showExceptionMessage(error);
                onLoadingDone();
            }
        }

        public void login(String userName, String password) {
            AuthStore.login(userName, password, new LoginResponse() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        AppSession.getInstance().setToken(response.getJSONObject("data").getString("token"));
                        Log.d("OBS", "is loading: " + isLoading());
                        if (isLoading()) getMe();
                    } catch (JSONException e) {
                        onLoadingDone();
                    }
                }
            });
        }

        private void getMe() {
            UserStore.getMe(new LoginResponse() {
                @Override
                public void onSuccess(JSONObject response) {
                    User user = UserStore.getData(response);

                    if (user != null) {
                        AppSession.getInstance().setUser(user);

                        if (isLoading()) getUserCourses();
                    } else {
                        AppSession.showDataLoadError("user");
                        onLoadingDone();
                    }
                }
            });
        }

        private void getUserCourses() {
            UserStore.getAllUserCourses(new LoginResponse() {
                @Override
                public void onSuccess(JSONObject response) {
                    ArrayList<Course> courses = CourseStore.getListData(response);

                    AppSession.getInstance().setUserCourses(courses);

                    if (isLoading()) getUserConexuses();
                }
            });
        }

        private void getUserConexuses() {
            UserStore.getAllUserConexuses(new LoginResponse() {
                @Override
                public void onSuccess(JSONObject response) {
                    ArrayList<Conexus> conexuses = ConexusStore.getListData(response);

                    AppSession.getInstance().setUserConexuses(conexuses);

                    if (isLoading()) pushMainActivity();
                }
            });
        }

        //convenience methods
        private void pushMainActivity() {
            getLoginActivity().pushMainActivity();
        }

        private boolean isLoading() {
            return getLoginActivity().isLoading();
        }

        private void onLoadingDone() {
            getLoginActivity().setLoading(false);
        }

        private LoginActivity getLoginActivity() {
            return (LoginActivity) getActivity();
        }
    }

    public boolean isLoading() {
        return pd.isShowing();
    }

    public void setLoading(boolean isLoading) {
        if (isLoading) pd.show();
        else pd.dismiss();

        loginBtn.setEnabled(!isLoading);
    }

    public void pushMainActivity() {
        Intent intent = new Intent(this, HomeFeedActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        pd.dismiss();
        AppSession.dismissToast();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("animation_completed", animationCompleted);
        outState.putBoolean("was_loading", pd.isShowing());
    }

    //used by login button's onClick() to determine contents of text fields
    private boolean hasFields() {
        boolean usernamePresent, passwordPresent;
        usernamePresent = usernameField != null && usernameField.getText().toString().length() > 0;
        passwordPresent = passwordField != null && passwordField.getText().toString().length() > 0;
        if (usernamePresent && passwordPresent) {
            return true;
        } else if (!usernamePresent && passwordPresent) {
            AppSession.showToast("Username cannot be blank");
        } else if (usernamePresent) {
            AppSession.showToast("Password cannot be blank");
        } else {
            AppSession.showToast("Fields cannot be blank");
        }
        return false;
    }

    private int fadeDuration = 500;

    private void fadeInLogo() {

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartOffset(1000);
        fadeIn.setDuration(fadeDuration);
        fadeIn.setFillAfter(true);
        fadeIn.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                resizeLogo();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        appLogoImage.startAnimation(fadeIn);

    }

    private int translateScaleDuration = 1000;

    //animates "CN" logo upon first opening the app
    private void resizeLogo() {

        float scaleX = (float) appLogoImageSmall.getHeight() / (float) appLogoImage.getHeight();
        float scaleY = (float) appLogoImageSmall.getWidth() / (float) appLogoImage.getWidth();

        int[] smallLogoPos = new int[2];
        appLogoImageSmall.getLocationOnScreen(smallLogoPos);

        int[] logoPos = new int[2];
        appLogoImage.getLocationOnScreen(logoPos);

        int deltaX = smallLogoPos[0] - logoPos[0];
        int deltaY = smallLogoPos[1] - logoPos[1];

        AnimationSet set = new AnimationSet(false);
        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0.0f,  // FromX
                TranslateAnimation.ABSOLUTE, deltaX,  // ToX
                TranslateAnimation.ABSOLUTE, 0.0f,  // FromY
                TranslateAnimation.ABSOLUTE, deltaY); // ToY
        translateAnimation.setDuration(translateScaleDuration);

        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, scaleX,
                1.0f, scaleY);
        scaleAnimation.setDuration(translateScaleDuration);
        scaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

        set.setFillAfter(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(translateAnimation);
        set.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeInView();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        appLogoImage.startAnimation(set);

    }

    private int fadeViewDuration = 1500;

    //fades in rest of fields after CN logo animation
    private void fadeInView() {

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(fadeViewDuration);
        fadeIn.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animationCompleted = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loginLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        loginLayout.startAnimation(fadeIn);

    }
}
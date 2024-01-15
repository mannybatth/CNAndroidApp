package com.thecn.app.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.MyActivities.MyActionBarActivity;
import com.thecn.app.fragments.BaseNotificationFragment;
import com.thecn.app.fragments.ColleagueRequestFragment;
import com.thecn.app.fragments.EmailNotificationFragment;
import com.thecn.app.fragments.NavigationDrawerFragment;
import com.thecn.app.fragments.NotificationFragment;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.Email;
import com.thecn.app.models.Picture;
import com.thecn.app.models.Post;
import com.thecn.app.models.User.User;
import com.thecn.app.models.UserNewMessage;
import com.thecn.app.services.UpdateService;
import com.thecn.app.services.UpdateService.NotificationBinder;
import com.thecn.app.stores.AuthStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.tools.PostChangeHandler;

import java.util.ArrayList;

public abstract class NavigationActivity extends MyActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    protected NavigationDrawerFragment mNavigationDrawerFragment;

    protected static final int CREATE_NEW_POST_REQUEST = 0;

    private String activityTitle = "";

    private SlidingMenu slidingMenu;

    private ViewPager mViewPager;
    private MyFragmentPagerAdapter mFragmentPagerAdapter;

    private View[] arrows;

    private boolean menuLastAction; //true for opened, false for closed

    private MenuItem notificationMenuButton;

    private RelativeLayout notificationButton, emailButton, requestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mySetContentView(); //subclasses may override

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        setUpSlidingMenu();
        setUpNotificationButtons();

        mViewPager = (ViewPager) findViewById(R.id.notif_pager);
        mViewPager.setOffscreenPageLimit(2);
        mFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), savedInstanceState);
        mViewPager.setAdapter(mFragmentPagerAdapter);
        mViewPager.setPageMarginDrawable(R.color.base_listview_background_color);
        mViewPager.setPageMargin(10);

        setUpArrows();

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setArrowVisibility(position);
                mFragmentPagerAdapter.focus(position);
            }
        });

        menuLastAction = false;

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mNotificationReceiver, new IntentFilter("onNotificationChange"));
    }

    protected BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setAllNotificationDisplays();
        }
    };

    public void updateCourses() {
        if (mService != null) {
            mService.setCourseUpdateNeeded(true);
        }
    }

    public void updateConexuses() {
        if (mService != null) {
            mService.setConexusUpdateNeeded(true);
        }
    }

    public void onStart() {
        super.onStart();

        Intent intent = new Intent(this, UpdateService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void onResume() {
        super.onResume();

        setAllNotificationDisplays();
    }

    public void onStop() {
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotificationReceiver);
    }

    private UpdateService mService;
    private boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            NotificationBinder binder = (NotificationBinder) iBinder;

            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    public UpdateService getUpdateService() {
        return mService;
    }

    private void setUpArrows() {
        arrows = new View[3];
        arrows[0] = findViewById(R.id.notification_arrow);
        arrows[1] = findViewById(R.id.email_arrow);
        arrows[2] = findViewById(R.id.colleague_arrow);


        arrows[0].setVisibility(View.VISIBLE);
        arrows[1].setVisibility(View.INVISIBLE);
        arrows[2].setVisibility(View.INVISIBLE);
    }

    private void setArrowVisibility(int index) {
        arrows[index].setVisibility(View.VISIBLE);

        for (int i = (index + 1) % 3; i != index; i = (i + 1) % 3) {
            arrows[i].setVisibility(View.INVISIBLE);
        }
    }

    private void setUpNotificationButtons() {
        notificationButton = (RelativeLayout) findViewById(R.id.notification_button_layout);
        emailButton = (RelativeLayout) findViewById(R.id.email_button_layout);
        requestButton = (RelativeLayout) findViewById(R.id.colleague_request_button_layout);

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() == 0) {
                    mFragmentPagerAdapter.focus(0);
                } else {
                    mViewPager.setCurrentItem(0);
                }
            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() == 1) {
                    mFragmentPagerAdapter.focus(1);
                } else {
                    mViewPager.setCurrentItem(1);
                }
            }
        });

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() == 2) {
                    mFragmentPagerAdapter.focus(2);
                } else {
                    mViewPager.setCurrentItem(2);
                }
            }
        });
    }

    private void setUpSlidingMenu() {
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.RIGHT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.drawer_shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset_2);
        slidingMenu.setFadeDegree(0.5f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(R.layout.notification_layout);
        slidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
            @Override
            public void onOpened() {
                if (!menuLastAction) {
                    int index = mViewPager.getCurrentItem();

                    mFragmentPagerAdapter.focus(index);

                    menuLastAction = true;
                }

                mNavigationDrawerFragment.closeDrawer();
            }
        });

        slidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
            @Override
            public void onClosed() {
                menuLastAction = false;
                mFragmentPagerAdapter.onMenuClose();
            }
        });
    }

    private static class MyFragmentPagerAdapter extends FragmentPagerAdapter{

        private BaseNotificationFragment[] fragments;

        FragmentManager mManager;

        private static final String NOTIFICATION_FRAGMENT_KEY = "notification_fragment";
        private static final String EMAIL_FRAGMENT_KEY = "email_fragment";
        private static final String REQUEST_FRAGMENT_KEY = "request_fragment";

        public MyFragmentPagerAdapter(FragmentManager fm, Bundle savedInstanceState) {
            super(fm);
            mManager = fm;

            fragments = new BaseNotificationFragment[3];

            if (savedInstanceState != null) {
                Fragment holder;

                holder = mManager.getFragment(savedInstanceState, NOTIFICATION_FRAGMENT_KEY);
                fragments[0] = holder != null ?
                        (BaseNotificationFragment) holder : new NotificationFragment();

                holder = mManager.getFragment(savedInstanceState, EMAIL_FRAGMENT_KEY);
                fragments[1] = holder != null ?
                        (BaseNotificationFragment) holder : new EmailNotificationFragment();

                holder = mManager.getFragment(savedInstanceState, REQUEST_FRAGMENT_KEY);
                fragments[2] = holder != null ?
                        (BaseNotificationFragment) holder : new ColleagueRequestFragment();
            } else {
                fragments[0] = new NotificationFragment();
                fragments[1] = new EmailNotificationFragment();
                fragments[2] = new ColleagueRequestFragment();
            }
        }

        @Override
        public Fragment getItem(int index) {
            return fragments[index];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        public void focus(int index) {
            fragments[index].onFocus();
        }

        public void onMenuClose() {
            for (BaseNotificationFragment f : fragments) {
                f.onMenuClose();
            }
        }

        public void onSaveInstanceState(Bundle outState) {
            if (fragments[0] != null) {
                mManager.putFragment(outState, NOTIFICATION_FRAGMENT_KEY, fragments[0]);
            }
            if (fragments[1] != null) {
                mManager.putFragment(outState, EMAIL_FRAGMENT_KEY, fragments[1]);
            }
            if (fragments[2] != null) {
                mManager.putFragment(outState, REQUEST_FRAGMENT_KEY, fragments[2]);
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mFragmentPagerAdapter.onSaveInstanceState(outState);
    }

    public void setSlidingEnabled(boolean enabled) {
        slidingMenu.setSlidingEnabled(enabled);
    }

    protected void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    public void setActivityTitle(String title) {
        this.activityTitle = title;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActionBarAndTitle(String title) {
        setActionBar();
        setActivityTitle(title);
        getActionBar().setTitle(activityTitle);
    }

    protected void mySetContentView() {
        setContentView(R.layout.activity_navigation);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, Object item) {

        if (item.equals("Home")) {

            openHomeFeedPage();

        } else if (item.equals("Profile")) {

            User user = AppSession.getInstance().getUser();
            openProfilePage(user);

        } else if (item.equals("Logout")) {

            logout();

        } else {

            if (item instanceof Course) {

                Course course = (Course) item;
                openCoursePage(course);

            } else if (item instanceof Conexus) {

                Conexus conexus = (Conexus) item;
                openConexusPage(conexus);

            }

        }
    }

    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
        } else if (slidingMenu.isMenuShowing()) {
            slidingMenu.showContent();
        } else {
            finish();
        }
    }

    public void closeDrawerIfOpen() {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
        }
    }

    protected interface OpenPageCallback {
        public void openActivity();
    }

    //allows for a drawer to close before page opened
    protected void openPage(final OpenPageCallback callback) {
        boolean menuOpen = false;

        if (mNavigationDrawerFragment.isDrawerOpen()) {
            menuOpen = true;
            setTouchable(false);
            mNavigationDrawerFragment.closeDrawer();
        } else if (slidingMenu.isMenuShowing()) {
            menuOpen = true;
            setTouchable(false);
            slidingMenu.showContent();
        }

        if (menuOpen) {
            long duration = getResources().getInteger(R.integer.close_drawer_duration);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setTouchable(true);
                    callback.openActivity();
                }
            }, duration);
        } else {
            callback.openActivity();
        }
    }

    protected void closeNotificationDrawer() {
        slidingMenu.showContent();
    }

    public void setTouchable(boolean touchable) {
        if (touchable) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public void openHomeFeedPage() {
        final Intent intent = new Intent(this, HomeFeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openPage(new OpenPageCallback() {
            @Override
            public void openActivity() {
                startActivity(intent);
            }
        });
    }

    public void openEmailPage(Email email) {
        final Intent intent = new Intent(this, EmailActivity.class);
        intent.putExtra("email", email);
        openPage(new OpenPageCallback() {
            @Override
            public void openActivity() {
                startActivity(intent);
            }
        });
    }

    public void openProfilePage(User user) {
        final Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user", user);
        openPage(new OpenPageCallback() {
            @Override
            public void openActivity() {
                startActivity(intent);
            }
        });
    }

    public void openProfilePage(String cnNumber) {
        final Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("cn_number", cnNumber);
        openPage(new OpenPageCallback() {
            @Override
            public void openActivity() {
                startActivity(intent);
            }
        });
    }

    public void openPostPage(Post post, boolean textFocus) {
        final Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("post", post);
        intent.putExtra("textFocus", textFocus);
        openPage(new OpenPageCallback() {
            @Override
            public void openActivity() {
                startActivity(intent);
            }
        });
    }

    public void openPollPage(Post post) {
        final Intent intent = new Intent(this, PollActivity.class);
        intent.putExtra("post", post);
        openPage(new OpenPageCallback() {
            @Override
            public void openActivity() {
                startActivity(intent);
            }
        });
    }

    public void openCoursePage(Course course) {
        final Intent intent = new Intent(this, CourseActivity.class);
        intent.putExtra("course", course);
        openPage(new OpenPageCallback() {
            @Override
            public void openActivity() {
                startActivity(intent);
            }
        });
    }

    public void openConexusPage(Conexus conexus) {
        final Intent intent = new Intent(this, ConexusActivity.class);
        intent.putExtra("conexus", conexus);
        openPage(new OpenPageCallback() {
            @Override
            public void openActivity() {
                startActivity(intent);
            }
        });
    }

    public void openPhotoGalleryViewerActivity(ArrayList<Picture> pics, int currentIndex) {
        Intent intent = new Intent(this, PhotoGalleryViewerActivity.class);
        intent.putExtra("pics", pics);
        intent.putExtra("currentIndex", currentIndex);
        startActivity(intent);
    }

    public void openPostLikesActivity(Post post) {
        Intent intent = new Intent(this, PostLikesActivity.class);
        intent.putExtra("post", post);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        boolean retVal;

        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.home, menu);
            retVal = true;
        } else {
            retVal = true;//super.onCreateOptionsMenu(menu);
        }

        setNotificationMenuButton(menu);

        return retVal;
    }

    public void setNotificationMenuButton(Menu menu) {
        notificationMenuButton = menu.findItem(R.id.action_notifications);
        if (notificationMenuButton != null) {
            notificationMenuButton.setActionView(R.layout.notification_button);

            final Menu finalMenu = menu;
            View actionView = notificationMenuButton.getActionView();
            if (actionView != null) {
                actionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finalMenu.performIdentifierAction(notificationMenuButton.getItemId(), 0);
                    }
                });
            }
        }

        setAllNotificationDisplays();
    }

    public synchronized void setAllNotificationDisplays() {
        UserNewMessage userNewMessage = AppSession.getInstance().getUserNewMessage();

        setTotalNotificationDisplay(userNewMessage.getTotal());
        setNotificationDisplay(userNewMessage.getNotificationCount());
        setEmailDisplay(userNewMessage.getEmailCount());
        setRequestDisplay(userNewMessage.getColleagueRequestCount());
    }

    private void setTotalNotificationDisplay(int count) {
        if (notificationMenuButton != null) {
            View actionView = notificationMenuButton.getActionView();

            if (actionView != null) {

                View indicator = actionView.findViewById(R.id.total_notification_indicator);
                TextView indicatorText = (TextView) actionView.findViewById(R.id.total_notification_indicator_text);

                setCountAndVisibility(count, indicator, indicatorText);
            }
        }
    }

    private void setNotificationDisplay(int count) {
        if (notificationButton != null) {

            View indicator = notificationButton.findViewById(R.id.notification_indicator);
            TextView indicatorText = (TextView) notificationButton.findViewById(R.id.notification_indicator_text);

            setCountAndVisibility(count, indicator, indicatorText);
        }
    }

    private void setEmailDisplay(int count) {
        if (emailButton != null) {

            View indicator = emailButton.findViewById(R.id.email_indicator);
            TextView indicatorText = (TextView) emailButton.findViewById(R.id.email_indicator_text);

            setCountAndVisibility(count, indicator, indicatorText);
        }
    }

    private void setRequestDisplay(int count) {
        if (requestButton != null) {

            View indicator = requestButton.findViewById(R.id.request_indicator);
            TextView indicatorText = (TextView) requestButton.findViewById(R.id.request_indicator_text);

            setCountAndVisibility(count, indicator, indicatorText);
        }
    }

    private void setCountAndVisibility(int count, View view, TextView textView) {
        String countText = count > 99 ? "99+" : Integer.toString(count);
        int visibility = count > 0 ? View.VISIBLE : View.INVISIBLE;

        view.setVisibility(visibility);
        textView.setText(countText);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_notifications) {
            slidingMenu.showMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        AuthStore.logout(new ResponseCallback() {});

        pushLoginActivity();
    }

    public void pushCreatePostActivity() {
        final Intent intent = new Intent(this, CreatePostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openPage(new OpenPageCallback() {
            @Override
            public void openActivity() {
                startActivityForResult(intent, CREATE_NEW_POST_REQUEST);
            }
        });
    }

    public void pushComposeEmailActivity() {
        final Intent intent = new Intent(this, ComposeEmailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openPage(new OpenPageCallback() {
            @Override
            public void openActivity() {
                startActivity(intent);
            }
        });
    }

    public void pushLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //login activity clears AppSession
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == CREATE_NEW_POST_REQUEST) {
            Post post = (Post) data.getSerializableExtra("NEW_POST");

            PostChangeHandler.sendAddedBroadcast(post);
        }
    }

    public void hideProgressBar() {
        findViewById(R.id.activityProgressBar).setVisibility(View.INVISIBLE);
    }

}

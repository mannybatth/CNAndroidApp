package com.thecn.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.thecn.app.AppSession;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.UserNewMessage;
import com.thecn.app.stores.ConexusStore;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.NewMessageStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.UserStore;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by philjay on 5/5/14.
 */
public class UpdateService extends Service {

    private final IBinder mBinder = new NotificationBinder();

    private static final int TEN_SECONDS = 10000;

    private boolean mCourseUpdateNeeded;
    private final Object courseUpdateLock = new Object();

    private boolean mConexusUpdateNeeded;
    private final Object conexusUpdateLock = new Object();

    private ArrayList<Updater> mUpdaters = new ArrayList<Updater>();
    private final Object updaterLock = new Object();

    private Timer mTimer;

    public class NotificationBinder extends Binder {
        public UpdateService getService() {
            return UpdateService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public interface Updater {
        public void update();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setCourseUpdateNeeded(true);
        setConexusUpdateNeeded(true);

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                NewMessageStore.getNewMessages(new ResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        UserNewMessage message = NewMessageStore.getData(response);

                        if (message != null) {
                            AppSession.getInstance().setUserNewMessage(message);
                        } else {
                            AppSession.getInstance().setUserNewMessage(new UserNewMessage());
                        }

                    }
                });

                if (isCourseUpdateNeeded()) {
                    UserStore.getAllUserCourses(new ResponseCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            ArrayList<Course> courses = CourseStore.getListData(response);

                            AppSession.getInstance().setUserCourses(courses);
                            setCourseUpdateNeeded(false);
                        }
                    });
                }

                if (isConexusUpdateNeeded()) {
                    UserStore.getAllUserConexuses(new ResponseCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            ArrayList<Conexus> conexuses = ConexusStore.getListData(response);

                            AppSession.getInstance().setUserConexuses(conexuses);
                            setConexusUpdateNeeded(false);
                        }
                    });
                }

                synchronized (updaterLock) {
                    for (Updater updater : mUpdaters) {
                        updater.update();
                    }
                }
            }

        }, 0, TEN_SECONDS);
    }

    public boolean isCourseUpdateNeeded() {
        synchronized (courseUpdateLock) {
            return mCourseUpdateNeeded;
        }
    }

    public void setCourseUpdateNeeded(boolean updateNeeded) {
        synchronized (courseUpdateLock) {
            mCourseUpdateNeeded = updateNeeded;
        }
    }

    public boolean isConexusUpdateNeeded() {
        synchronized (conexusUpdateLock) {
            return mConexusUpdateNeeded;
        }
    }

    public void setConexusUpdateNeeded(boolean updateNeeded) {
        synchronized (conexusUpdateLock) {
            mConexusUpdateNeeded = updateNeeded;
        }
    }

    public void addUpdater(Updater updater) {
        synchronized (updaterLock) {
            mUpdaters.add(updater);
        }
    }

    public void removeUpdater(Updater updater) {
        synchronized (updaterLock) {
            mUpdaters.remove(updater);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mTimer != null) {
            mTimer.cancel();
        }
    }
}

package com.mycheez.application;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by ahetawal on 7/4/15.
 */
public class MyCheezApplication extends Application {
    public static final String LOG_TAG = "stealcheese";
    public static final String PIN_TAG = "appData";
    public static final String PROFILE_PIC_URL = "https://graph.facebook.com/%s/picture";
    public static final String FRIEND_CHEESE_COUNT_PIC_URL = "https://graph.facebook.com/%S/picture?type=normal";
    public static final String FRIEND_HISTORY_PIC_URL = "https://graph.facebook.com/%S/picture?type=small";
    private static boolean activityRunning;
    private static boolean activityPaused;

    @Override
    public void onCreate() {
        super.onCreate();

        // setup Firebase
        Firebase.setAndroidContext(this);

        // TODO: move to string.xml firebase url
        //Firebase rootRef = new Firebase("https://torrid-inferno-8611.firebaseio.com/TODO");
     }

    public static boolean isActivityRunning() {
        return activityRunning;
    }


    public static void setActivityisStopping() {
        activityRunning = false;

    }

    public static void setActivityisStillRunning() {
        activityRunning = true;

    }


    public static boolean isActivityPaused() {
        return activityPaused;
    }


    public static void setActivityUnPaused() {
        activityPaused = false;

    }

    public static void setActivityPaused() {
        activityPaused = true;

    }

}

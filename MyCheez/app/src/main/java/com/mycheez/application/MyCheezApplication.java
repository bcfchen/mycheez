package com.mycheez.application;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;
import com.mycheez.model.User;

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
    private static Firebase rootFirebaseRef;
    private static Firebase myCheezFirebaseRef;
    private static Firebase userPresenceRef;

    private static User currentUser;

    @Override
    public void onCreate() {
        super.onCreate();

        // setup Firebase and Facebook
        FacebookSdk.sdkInitialize(this);
        Firebase.setAndroidContext(this);

        rootFirebaseRef = new Firebase("https://torrid-inferno-8611.firebaseio.com");
        myCheezFirebaseRef = new Firebase("https://torrid-inferno-8611.firebaseio.com/mycheez");
        userPresenceRef = new Firebase("https://torrid-inferno-8611.firebaseio.com/.info/connected");


     }


    public static Firebase getRootFirebaseRef() {
        return rootFirebaseRef;
    }

    public static Firebase getMyCheezFirebaseRef() {
        return myCheezFirebaseRef;
    }

    public static Firebase getUserPresenceRef() {
        return userPresenceRef;
    }



    public static boolean isActivityRunning() {
        return activityRunning;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        MyCheezApplication.currentUser = currentUser;

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

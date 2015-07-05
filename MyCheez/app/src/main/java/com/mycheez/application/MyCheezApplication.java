package com.mycheez.application;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by ahetawal on 7/4/15.
 */
public class MyCheezApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        // setup Firebase
        Firebase.setAndroidContext(this);

        // TODO: move to string.xml firebase url
        //Firebase rootRef = new Firebase("https://torrid-inferno-8611.firebaseio.com/TODO");
     }

}

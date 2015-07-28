package com.mycheez.gcm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by ahetawal on 7/27/15.
 */
public class GcmRegistrationHelper {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final Activity activity;
    private static final String TAG = "gcmRegistrationHelper";


    public GcmRegistrationHelper(Activity activity) {
        this.activity = activity;
    }


    public void registerGcmIfNecessary() {
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(activity, RegistrationIntentService.class);
            activity.startService(intent);
        } else {
            Log.e(TAG, "No valid Google Play Services APK found.");
            showNotSupportedDialog();
        }

    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil
                        .getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.e(TAG, "This device is not supported.");
                showNotSupportedDialog();
            }
            return false;
        }
        return true;
    }

    private void showNotSupportedDialog() {
        new AlertDialog.Builder(activity)
                .setMessage("Sorry, your device is not supported. Please contact mycheez@gmail.com for assistance.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                })
                .show();
    }
}

package com.mycheez.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mycheez.gcm.GcmPreferencesContants;

public class SharedPreferencesService {
    private SharedPreferences sharedPreferences;

    public SharedPreferencesService(Context context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Boolean toggleNotificationSetting(){
        Boolean notificationSetting = sharedPreferences.getBoolean(GcmPreferencesContants.NOTIFICATION_SETTING, true);
        Boolean updatedNotificationSetting = !notificationSetting;
        sharedPreferences.edit().putBoolean(GcmPreferencesContants.NOTIFICATION_SETTING, updatedNotificationSetting).apply();

        return updatedNotificationSetting;
    }

    public void saveUserIdToSharedPreferences(String facebookId) {
        sharedPreferences.edit().putString(GcmPreferencesContants.USER_ID_SHARED_PREF_KEY, facebookId).apply();
    }

    public String getUserIdToSharedPreferences() {
        String facebookId = sharedPreferences.getString(GcmPreferencesContants.USER_ID_SHARED_PREF_KEY, null);
        return facebookId;
    }

    public Boolean getNotificationSetting(){
        return sharedPreferences.getBoolean(GcmPreferencesContants.NOTIFICATION_SETTING, true);
    }
}

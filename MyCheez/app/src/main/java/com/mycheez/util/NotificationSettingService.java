package com.mycheez.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mycheez.gcm.GcmPreferencesContants;

public class NotificationSettingService {
    private SharedPreferences sharedPreferences;

    public NotificationSettingService(Context context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Boolean toggleNotificationSetting(){
        Boolean notificationSetting = sharedPreferences.getBoolean(GcmPreferencesContants.NOTIFICATION_SETTING, true);
        Boolean updatedNotificationSetting = !notificationSetting;
        sharedPreferences.edit().putBoolean(GcmPreferencesContants.NOTIFICATION_SETTING, updatedNotificationSetting).apply();

        return updatedNotificationSetting;
    }

    public void setNotificationSetting(Boolean enable){
        sharedPreferences.edit().putBoolean(GcmPreferencesContants.NOTIFICATION_SETTING, enable).apply();
    }

    public Boolean getNotificationSetting(){
        return sharedPreferences.getBoolean(GcmPreferencesContants.NOTIFICATION_SETTING, true);
    }
}

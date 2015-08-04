package com.mycheez.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.mycheez.R;
import com.mycheez.enums.CheeseCountChangeType;

public class TheftSoundsPlayer {
    private MediaPlayer mediaPlayer;

    public void playSoundOnCheeseChange(Context context, CheeseCountChangeType changeType){
        // don't do anything if isUserThief is null
        if (changeType == null){ return;}

        switch(changeType){
            case STEAL:
                playSoundOnSteal(context);
                break;
            case STOLEN:
                playSoundOnStolen(context);
                break;
            case NO_CHANGE:
                break;
        }
    }

    /* play sound when user steals someone's cheese */
    private void playSoundOnSteal(Context context){
        mediaPlayer = MediaPlayer.create(context, R.raw.chime_bell_ding);
        playAudioOrVibrate(context, mediaPlayer);
    }


    /* play sound when someone steals user's cheese */
    private void playSoundOnStolen(Context context){
        mediaPlayer = MediaPlayer.create(context, R.raw.pop_drip);
        playAudioOrVibrate(context, mediaPlayer);
    }

    /* either play sound or vibrate phone depending on user's phone settings */
    private static void playAudioOrVibrate(Context context, MediaPlayer mediaPlayer){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        switch (audioManager.getRingerMode()){
            case AudioManager.RINGER_MODE_VIBRATE:
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(250);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                setupOnCompletion(mediaPlayer);
                mediaPlayer.start();
                break;
        }
    }

    private static void setupOnCompletion(MediaPlayer mediaPlayer){
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
            }

        });
    }

}

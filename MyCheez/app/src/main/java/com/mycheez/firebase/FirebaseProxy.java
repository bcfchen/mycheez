package com.mycheez.firebase;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.mycheez.application.MyCheezApplication;
import com.mycheez.model.User;

import java.util.Date;

/**
 * Created by ahetawal on 7/19/15.
 */
public class FirebaseProxy  {

    private static final String TAG = "firebaseproxy";
    private static Firebase myCheezRef = MyCheezApplication.getMyCheezFirebaseRef();

    public static void upsertCurrentUser(final User currentUser, final UpsertUserCallBack callback){

        myCheezRef.child("users").child(currentUser.getFacebookId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Log.i(TAG, "user exists, update it");
                    User userOnFirebase = snapshot.getValue(User.class);
                    Log.i(TAG, "Firebase User is : " + userOnFirebase);
                    //IMPORTANT : Leave the cheese count and creation as it is.
                    currentUser.setCheeseCount(userOnFirebase.getCheeseCount());
                    currentUser.setCreatedAt(userOnFirebase.getCreatedAt());
                    updateCurrentUser(currentUser, callback);
                } else {
                    Log.i(TAG, "user does not exist, insert it");
                    currentUser.setCreatedAt(new Date());
                    insertNewUser(currentUser, callback);
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
                Log.e(TAG, "Error upserting: " + error);
            }
        });
    }


    public static void insertNewUser(User currentuser, final UpsertUserCallBack callBack){
        Firebase currentUserRef = myCheezRef.child("users").child(currentuser.getFacebookId());
        currentUserRef.setValue(currentuser, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    callBack.isUpsertSuccess(false);
                    Log.e(TAG, "Error inserting: " + firebaseError);
                } else {
                    callBack.isUpsertSuccess(true);
                }
            }
        });

    }

    public static void updateCurrentUser(User currentuser, final UpsertUserCallBack callBack){
        Firebase currentUserRef = myCheezRef.child("users").child(currentuser.getFacebookId());
        currentUserRef.setValue(currentuser, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    callBack.isUpsertSuccess(false);
                    Log.e(TAG, "Error updating: " + firebaseError);
                } else {
                    callBack.isUpsertSuccess(true);
                }
            }
        });

    }




    public interface UpsertUserCallBack {
        void isUpsertSuccess(boolean result);
    }
}

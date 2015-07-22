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

    public static void getUserData(String facebookId, final GetUserDataCallback callback){
        myCheezRef.child("users").child(facebookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.i(TAG, "user loaded from Firebase");
                User currentUser = snapshot.getValue(User.class);
                callback.userDataRetrieved(currentUser);
            }

            @Override
            public void onCancelled(FirebaseError error) {
                Log.i(TAG, "loading user from Firebase failed: " + error.toString());
                callback.userDataRetrieved(null);
            }
        });
    }

    public static void getUserCheeseCount(String facebookId, final GetUserCheeseCountCallback callback){
        myCheezRef.child("users").child(facebookId).child("cheeseCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.i(TAG, "cheese count changed in Firebase");
                Integer updatedCheeseCount = snapshot.getValue(Integer.class);
                callback.userCheeseCountRetrieved(updatedCheeseCount);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.i(TAG, "failed to get user cheesecount from Firebase");
                callback.userCheeseCountRetrieved(null);
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

    public interface GetUserCheeseCountCallback{
        void userCheeseCountRetrieved(Integer cheeseCount);
    }

    public interface GetUserDataCallback{
        void userDataRetrieved(User user);
    }

    public interface UpsertUserCallBack {
        void isUpsertSuccess(boolean result);
    }
}

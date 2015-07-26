package com.mycheez.firebase;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.mycheez.application.MyCheezApplication;
import com.mycheez.model.History;
import com.mycheez.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ahetawal on 7/19/15.
 */
public class FirebaseProxy  {
    private static final String TAG = "firebaseproxy";
    private static Firebase myCheezRef = MyCheezApplication.getMyCheezFirebaseRef();

    /**
     * Firebase operation to update or insert a user.
     * This operation is performed when the new user opens the app for the very first time
     * OR
     * When an existing user opens the app
     * We try to update Firebase with the latest info for that user at that point in time.
     * @param currentUser
     * @param callback
     */
    public static void upsertCurrentUser(final User currentUser, final UpsertUserCallBack callback){
        myCheezRef.child("users").child(currentUser.getFacebookId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Log.i(TAG, "user exists, update it");
                    User userOnFirebase = snapshot.getValue(User.class);
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
        currentUserRef.setValue(currentuser, currentuser.getCheeseCount(), new Firebase.CompletionListener() {
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
        currentUserRef.setValue(currentuser, currentuser.getCheeseCount(), new Firebase.CompletionListener() {
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

    /**
     * SingleValueEvent triggered when we land on to Theftactivity,
     * We use it for getting the latest cheese count and other stuff for the user
     *
     * NOTE: This forms the basis of the static user object which all functions read from.
     *
     * @param facebookId
     * @param callback
     */
    public static void getUserData(String facebookId, final UserDataCallback callback){
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


    /**
     * ValueListener on cheeseCount property of current User.
     * Used for updating the cheese Counter for current user, if others are stealing from it.
     * Or the user itself is performing a steal
     * @param facebookId
     * @param callback
     */
    public static void getUserCheeseCount(String facebookId, final UserCheeseCountCallback callback){
        myCheezRef.child("users").child(facebookId).child("cheeseCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
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

    public static void getUserRanking(final String userFacebookId, final UserRankingCallback callback){
        myCheezRef.child("users").orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.i(TAG, "retrieved all users");
                Map<String, Object> users = (Map<String, Object>) snapshot.getValue();
                Integer pos = new ArrayList<>(users.keySet()).indexOf(userFacebookId);
                HashMap retrievedUser = (HashMap) users.get(userFacebookId);
                User currentUser = new User();
                currentUser.setFirstName(retrievedUser.get("firstName").toString());
                currentUser.setCheeseCount(Integer.parseInt(retrievedUser.get("cheeseCount").toString()));
                currentUser.setProfilePicUrl(retrievedUser.get("profilePicUrl").toString());
                callback.userRankingRetrieved(pos + 1, currentUser);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "The read failed: " + firebaseError.getMessage());
                callback.userRankingRetrieved(null, null);
            }
        });
    }


    /**
     * Firebase operation to insert the audit log of the theft history from the current user
     * @param victimId
     */
    public static void insertTheftHistory(String victimId){
        String currentUserName = MyCheezApplication.getCurrentUser().getFirstName();
        Firebase currentUserRef = myCheezRef.child("history").child(victimId);
        History hist = new History();
        hist.setThiefName(currentUserName);
        currentUserRef.push().setValue(hist);

    }

    /**
     * <b>MAIN: </b>Firebase operatin to perform cheese theft update and calculations
     * Sequence:
     * 1. First reduce cheese for victim if that is success,
     * 2. Give the new cheese to the thief
     * 3. Also look at the validation rules on Firebase for negative cheeseCounts.
     *
     * @param victim
     */
    public static void doCheeseTheft(final User victim, final CheeseTheftActionCallback theftCallback){

        final User thief = MyCheezApplication.getCurrentUser();
        Firebase victimRef = myCheezRef.child("users").child(victim.getFacebookId());
        Map<String, Object> victimCheeseCountMap = new HashMap<String, Object>();
        victimCheeseCountMap.put("cheeseCount", victim.getCheeseCount()-1);
        victimCheeseCountMap.put("updatedAt", ServerValue.TIMESTAMP);
        victimRef.setPriority(victim.getCheeseCount()-1);
        victimRef.updateChildren(victimCheeseCountMap, new Firebase.CompletionListener(){
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if(firebaseError == null){
                    Firebase currentUserRef = myCheezRef.child("users").child(thief.getFacebookId());
                    Map<String, Object> thiefCheeseCountMap = new HashMap<String, Object>();
                    thiefCheeseCountMap.put("cheeseCount", (thief.getCheeseCount() + 1));
                    thiefCheeseCountMap.put("updatedAt", ServerValue.TIMESTAMP);
                    currentUserRef.setPriority(thief.getCheeseCount() + 1);
                    currentUserRef.updateChildren(thiefCheeseCountMap);
                    theftCallback.cheeseTheftPerformed(true);
                } else {
                    Log.e(TAG, "Error performing theft: " + firebaseError.getMessage());
                    theftCallback.cheeseTheftPerformed(false);
                }
            }
        });
    }

    /**
     * Firebase operation to track an user's presence, if playing MyCheez.
     * On closing the app or disconnecting we update the online status to false
     * This status to be used by Node server to send out notifications
     * @param current
     */
    public static void setupUserPresence(User current){
        Firebase firebaseConnectedRef = MyCheezApplication.getUserPresenceRef();
        final Firebase userPresenceRefStatus = myCheezRef.child("presence").child(current.getFacebookId()).child("isOnline");
        final Firebase userPresenceRefUpdatedAt = myCheezRef.child("presence").child(current.getFacebookId()).child("updatedAt");

        final Firebase currentUserPresenceRef = myCheezRef.child("users").child(current.getFacebookId()).child("isOnline");

        firebaseConnectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.i(TAG, "Device connected ");
                    userPresenceRefStatus.setValue(Boolean.TRUE);
                    currentUserPresenceRef.setValue((Boolean.TRUE));

                    currentUserPresenceRef.onDisconnect().setValue(Boolean.FALSE);
                    userPresenceRefStatus.onDisconnect().setValue(Boolean.FALSE);
                    userPresenceRefUpdatedAt.onDisconnect().setValue(ServerValue.TIMESTAMP);
                } else {
                    Log.i(TAG, "Device dis-connected ");
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
                System.err.println("Listener was cancelled");
            }
        });



    }

    /* All callback interfaces for signalling completion of the firebase operatios */

    public interface UserRankingCallback{
        void userRankingRetrieved(Integer rank, User currentUser);
    }

    public interface UserCheeseCountCallback{
        void userCheeseCountRetrieved(Integer cheeseCount);
    }

    public interface UserDataCallback{
        void userDataRetrieved(User user);
    }

    public interface UpsertUserCallBack {
        void isUpsertSuccess(boolean result);
    }

    public interface CheeseTheftActionCallback {
        void cheeseTheftPerformed(boolean isSuccess);
    }
}

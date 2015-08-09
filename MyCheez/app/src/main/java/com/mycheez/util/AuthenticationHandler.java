package com.mycheez.util;

import android.content.Intent;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.mycheez.application.MyCheezApplication;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/* class that handles facebook/firebase authentication */
public class AuthenticationHandler {
    private Firebase mFirebaseRef;
    private CallbackManager mFacebookCallbackManager;
    private AccessTokenTracker mFacebookAccessTokenTracker;
    private AuthData mAuthData;
    private final String TAG = "authenticationHandler";
    private FacebookAuthenticationValidated facebookAuthCallback;
    private FirebaseAuthenticationValidated firebaseAuthCallback;


    public void initialize(FacebookAuthenticationValidated facebookAuthCallback, FirebaseAuthenticationValidated firebaseAuthCallback){
        this.facebookAuthCallback = facebookAuthCallback;
        this.firebaseAuthCallback = firebaseAuthCallback;

        initializeFacebookAuthentication();
        initializeFirebaseAuthentication();
    }

    public void initializeFacebookAuthentication(){
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                onFacebookAccessTokenChange(currentAccessToken);
            }
        };

    }

    public AccessTokenTracker getmFacebookAccessTokenTracker() {
        return mFacebookAccessTokenTracker;
    }

    public void initializeFirebaseAuthentication(){
        mFirebaseRef = MyCheezApplication.getMyCheezFirebaseRef();
    }

    public void triggerOnActivityResult(int requestCode, int resultCode, Intent data){
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void onFacebookAccessTokenChange(AccessToken token) {
        if (token != null) {
            System.out.println("About to call : " + new Date());
            facebookAuthCallback.facebookAuthenticationValidated(true);
            mFirebaseRef.authWithOAuthToken("facebook", token.getToken(), new AuthResultHandler("facebook"));
        } else {
            // Logged out of Facebook and currently authenticated with Firebase using Facebook, so do a logout
            if (mAuthData != null && mAuthData.getProvider().equals("facebook")) {
                mFirebaseRef.unauth();
            }
        }
    }

    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private final String provider;

        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            Log.i(TAG, "authentication success");
            mAuthData = authData;
            Log.i(TAG, "Auth data is : " + mAuthData);

            firebaseAuthCallback.firebaseAuthenticationValidated(true, mAuthData);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            Log.e(TAG, "authentication failed with error: " + firebaseError.getDetails());
            firebaseAuthCallback.firebaseAuthenticationValidated(false, null);
        }
    }

    /* call firebase and facebook to see if credentials are still valid, or have expired */
    public void validateUserAuthentication(final UserAuthenticationValidated callback){
        AuthData firebaseAuthData = mFirebaseRef.getAuth();

        // declare user credentials invalid if firebase creds not valid
        if (firebaseAuthData == null){
            callback.userAuthenticationValidated(false, null);
            return;
        }
        authUserAndGetFacebookFriendsList(facebookAuthenticateCallback(callback));

    }

    private GraphRequest.GraphJSONArrayCallback facebookAuthenticateCallback(final UserAuthenticationValidated userAuthenticatedCallback) {
        GraphRequest.GraphJSONArrayCallback graphApiCallback = new GraphRequest.GraphJSONArrayCallback() {
            @Override
            public void onCompleted(JSONArray jsonArray, GraphResponse response) {
                List<String> friendsList = new ArrayList<>();
                // invoke callback with true/false based on authentication response from facebook
                if (response.getError() == null){
                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            // get friends Facebook ids
                            friendsList.add(jsonArray.getJSONObject(i).getString("id"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing friends request ", e);
                    }
                    userAuthenticatedCallback.userAuthenticationValidated(true, friendsList);
                } else {
                    Log.e(TAG, "Facebook error response Type : " + response.getError().getErrorType());
                    Log.e(TAG, "Facebook error response details : " + response.getError().getErrorMessage());
                    userAuthenticatedCallback.userAuthenticationValidated(false, null);
                }
            }
        };

        return graphApiCallback;
    }

    public void authUserAndGetFacebookFriendsList(GraphRequest.GraphJSONArrayCallback callback){
        GraphRequest request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), callback);
        request.executeAsync();
    }

    public interface FirebaseAuthenticationValidated{
        void firebaseAuthenticationValidated(Boolean isValid, AuthData authData);
    }

    public interface FacebookAuthenticationValidated{
        void facebookAuthenticationValidated(Boolean isValid);
    }

    public interface UserAuthenticationValidated{
        void userAuthenticationValidated(Boolean isValid, List<String> friendsList);
    }


}

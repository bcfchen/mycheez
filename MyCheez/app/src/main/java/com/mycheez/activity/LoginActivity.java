package com.mycheez.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.mycheez.R;
import com.mycheez.application.MyCheezApplication;
import com.mycheez.firebase.FirebaseProxy;
import com.mycheez.model.User;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LoginActivity extends Activity {

    private TextView loadingText;
    private LoginButton loginFBButton;
    private double timeLeft = 0d;
    private Firebase mFirebaseRef;
    private CallbackManager mFacebookCallbackManager;
    /* Used to track user logging in/out off Facebook */
    private AccessTokenTracker mFacebookAccessTokenTracker;
    /* Data from the authenticated user */
    private AuthData mAuthData;
    private Firebase.AuthStateListener mAuthStateListener;
    private LinearLayout titleContainer;
    private LinearLayout loadingMsgSection;
    private static final String TAG = "loginActivity";
    private User currentUser = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initialize() {
        // initialize layouts
        initializeUIComponents();
        // initialize Firebase reference
        mFirebaseRef = MyCheezApplication.getRootFirebaseRef();
        initializeFirebaseAuth();
        initializeFacebookLogin();
    }

    private void initializeUIComponents(){
        loginFBButton = (LoginButton) findViewById(R.id.loginButton);
        loginFBButton.setVisibility(View.GONE);
        loadingMsgSection = (LinearLayout) findViewById(R.id.loadingMsgSection);
        loadingMsgSection.setVisibility(View.GONE);
        loadingText = (TextView) findViewById(R.id.loadingText);
        titleContainer = (LinearLayout) findViewById(R.id.titleContainer);
    }

    private void initializeFirebaseAuth(){
        mAuthStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                mAuthData = authData;
                Log.i(TAG, "Auth data is : " + mAuthData);
                Toast.makeText(LoginActivity.this, "I am getting called", Toast.LENGTH_LONG).show();
                doLoginAnimation();
             }
        };
        /* Check if the user is authenticated with Firebase already. If this is the case we can set the authenticated
         * user and hide hide any login buttons */
        mFirebaseRef.addAuthStateListener(mAuthStateListener);
    }

    private void initializeFacebookLogin(){
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                onFacebookAccessTokenChange(currentAccessToken);
            }
        };
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
    }


    private void doLoginAnimation(){
        Animation animTranslate  = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.translate);
        animTranslate.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                if (mAuthData != null) {
                    setAuthenticatedUser();
                } else {
                    /* no user authenticated with Firebase
                     * so display login button */
                    LoginManager.getInstance().logOut();
                    loginFBButton.setVisibility(View.VISIBLE);
                    Animation animFade = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
                    loginFBButton.startAnimation(animFade);
                }
            }
        });
        titleContainer.startAnimation(animTranslate);
    }

    private void onFacebookAccessTokenChange(AccessToken token) {
        if (token != null) {
            System.out.println("About to call : " + new Date());
            mFirebaseRef.authWithOAuthToken("facebook", token.getToken(), new AuthResultHandler("facebook"));
        } else {
            // Logged out of Facebook and currently authenticated with Firebase using Facebook, so do a logout
            if (this.mAuthData != null && this.mAuthData.getProvider().equals("facebook")) {
                mFirebaseRef.unauth();
            }
        }
    }

    /**
     * Once a user is logged in, take the mAuthData provided from Firebase and "use" it.
     * 1. Populates basic profile info of user
     * 2. Call Facebook graph api to get friends list
     * 3. Upsert current user in Firebase
     */
    private void setAuthenticatedUser() {
        if(mAuthData !=null) {
            populateProfileInfoForUser();
            GraphRequest meFriendsListRequest = generateFriendListRequest();
            GraphRequestBatch batch = new GraphRequestBatch(meFriendsListRequest);
            batch.addCallback(new GraphRequestBatch.Callback() {
                @Override
                public void onBatchCompleted(GraphRequestBatch graphRequests) {
                    Log.i(TAG, "All requests completed. User is:  " + currentUser);

                    // Save to Firebase
                    FirebaseProxy.upsertCurrentUser(currentUser, new FirebaseProxy.UpsertUserCallBack() {
                        @Override
                        public void isUpsertSuccess(boolean isSuccess) {
                            Log.i(TAG, "Completed");
                            // Based on Success flag
                            // Move to theftactivity or show some popup

                        }
                    });
                }
            });
            batch.executeAsync();
        }

    }

    /**
     * Facebook graph api call to get Friends list of current user
     * @return
     */
    private GraphRequest generateFriendListRequest() {
        return GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONArrayCallback() {
                            @Override
                            public void onCompleted(JSONArray jsonArray, GraphResponse response) {
                                List<String> list = new ArrayList<>();
                                try {
                                    for(int i = 0; i < jsonArray.length(); i++){
                                        // get friends Facebook ids
                                       list.add(jsonArray.getJSONObject(i).getString("id"));
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error parsing friends request ", e);
                                }
                                currentUser.setFriends(list);
                            }
                        });
    }

    /**
     * Method used for populating basic profile info of current user
     * from auth data
     */
    private void populateProfileInfoForUser() {
        currentUser.setFacebookId((String) mAuthData.getProviderData().get("id"));
        currentUser.setIsOnline(true);
        Map<String,Object> userProfileData = (Map)mAuthData.getProviderData().get("cachedUserProfile");
        currentUser.setFirstName((String)userProfileData.get("first_name"));
        currentUser.setLastName((String)userProfileData.get(("last_name")));
        Map<String, Object> pictureData = (Map)userProfileData.get(("picture"));
        currentUser.setProfilePicUrl((String)((Map)pictureData.get("data")).get("url"));
    }

    /**
     * Utility class for authentication results
     */
    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private final String provider;

        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            Log.i(TAG, "authentication success");
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            Log.e(TAG, "authentication failed with error: " + firebaseError.getDetails());
        }
    }

    private void showLoadingMsgSection(String message) {
        loadingMsgSection.setVisibility(View.VISIBLE);
        Animation animFade  = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
        loadingMsgSection.startAnimation(animFade);
        loadingText.setText(message);
    }

    private void hideLoadingMsgSection() {
        loadingMsgSection.setVisibility(View.GONE);
    }

    private void checkNetworkAvailability() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            Toast.makeText(this, R.string.no_network_message, Toast.LENGTH_LONG).show();
            startTheftActivity();
        }
    }

    private void startTheftActivity() {
        Intent intent = new Intent(LoginActivity.this, TheftActivity.class);
        intent.putExtra("facebookId",currentUser.getFacebookId());
        startActivity(intent);
        finish();
    }



}

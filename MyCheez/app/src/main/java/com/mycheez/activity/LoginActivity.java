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
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.mycheez.R;
import com.mycheez.application.MyCheezApplication;

import java.util.Date;

public class LoginActivity extends Activity {

    private TextView loadingText;
    private LoginButton loginFBButton;
    private double timeLeft = 0d;
    private Firebase firebaseRef;
    private CallbackManager mFacebookCallbackManager;
    /* Used to track user logging in/out off Facebook */
    private AccessTokenTracker mFacebookAccessTokenTracker;
    /* Data from the authenticated user */
    private AuthData mAuthData;
    private Firebase.AuthStateListener mAuthStateListener;
    private LinearLayout titleContainer;
    private  LinearLayout loadingMsgSection;
    private String TAG = "loginActivity";

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


    private void initialize() {
        // initialize layouts
        initializeUIComponents();
        // initialize Firebase reference
        firebaseRef = MyCheezApplication.getRootFirebaseRef();
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
                Toast.makeText(LoginActivity.this, "I am getting called", Toast.LENGTH_LONG).show();
                mAuthData = authData;
                doLoginAnimation();
             }
        };
        /* Check if the user is authenticated with Firebase already. If this is the case we can set the authenticated
         * user and hide hide any login buttons */
        firebaseRef.addAuthStateListener(mAuthStateListener);
    }

    private void initializeFacebookLogin(){
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                onFacebookAccessTokenChange(currentAccessToken);
            }
        };
    }


    private void onFacebookAccessTokenChange(AccessToken token) {
        if (token != null) {
            System.out.println("About to call : " + new Date());
            firebaseRef.authWithOAuthToken("facebook", token.getToken(), new AuthResultHandler("facebook"));
        } else {
            // Logged out of Facebook and currently authenticated with Firebase using Facebook, so do a logout
            if (this.mAuthData != null && this.mAuthData.getProvider().equals("facebook")) {
                firebaseRef.unauth();
            }
        }
    }

    /**
     * Once a user is logged in, take the mAuthData provided from Firebase and "use" it.
     */
    private void setAuthenticatedUser() {
        if(mAuthData !=null) {
            // Update firebase with latest timstamp for this user, and update fb graph api call..
            // move to theft activity..
        }

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
            Log.e(TAG, "authentication failed");
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
        intent.putExtra("authenticationUid", mAuthData.getUid());
        //startActivity(intent);
       // finish();
    }



}

package com.mycheez.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.mycheez.R;
import com.mycheez.application.MyCheezApplication;

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


    private  LinearLayout loadingMsgSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginFBButton = (LoginButton) findViewById(R.id.loginButton);
        loginFBButton.setVisibility(View.GONE);


        loadingMsgSection = (LinearLayout) findViewById(R.id.loadingMsgSection);
        loadingMsgSection.setVisibility(View.GONE);

        loadingText = (TextView) findViewById(R.id.loadingText);
        initialize();

        Animation animTranslate  = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.translate);
        animTranslate.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) { }

            @Override
            public void onAnimationRepeat(Animation arg0) { }

            @Override
            public void onAnimationEnd(Animation arg0) {
                /* check auth and decide to show login button or not */
                    AuthData authData = firebaseRef.getAuth();
                    if (authData != null) {
                        // user authenticated with Firebase
                    } else {
                    /* no user authenticated with Firebase
                     * so display login button */
                        loginFBButton.setVisibility(View.VISIBLE);
                        Animation animFade = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
                        loginFBButton.startAnimation(animFade);
                }
            }
        });
            LinearLayout titleContainer = (LinearLayout) findViewById(R.id.titleContainer);
            titleContainer.startAnimation(animTranslate);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initialize() {
        // initialize Firebase reference
        firebaseRef = MyCheezApplication.getRootFirebaseRef();
        initializeFirebaseAuth();
        initializeFacebookLogin();
    }

    private void initializeFirebaseAuth(){
        mAuthStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                setAuthenticatedUser(authData);
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
            firebaseRef.authWithOAuthToken("facebook", token.getToken(), new AuthResultHandler("facebook"));
        } else {
            // Logged out of Facebook and currently authenticated with Firebase using Facebook, so do a logout
            if (this.mAuthData != null && this.mAuthData.getProvider().equals("facebook")) {
                firebaseRef.unauth();
                setAuthenticatedUser(null);
            }
        }
    }

    /**
     * Once a user is logged in, take the mAuthData provided from Firebase and "use" it.
     */
    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            /* Hide all the login buttons */
            //loginFBButton.setVisibility(View.GONE);
            // START THEFT ACTIVITY HERE!!
          //  startTheftActivity();

        } else {
            /* No authenticated user show all the login buttons */
            //loginFBButton.setVisibility(View.VISIBLE);
        }
        this.mAuthData = authData;
        /* invalidate options menu to hide/show the logout button */
        //supportInvalidateOptionsMenu();
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

            setAuthenticatedUser(authData);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            int i = 2;
            System.out.println("See me");
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
//        intent.putExtra("CountDown", timeLeft);
        startActivity(intent);
        finish();
    }



}

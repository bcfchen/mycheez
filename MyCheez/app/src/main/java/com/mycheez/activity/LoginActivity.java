package com.mycheez.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.facebook.Request;
//import com.facebook.Response;
//import com.facebook.model.GraphUser;
//import com.parse.FunctionCallback;
//import com.parse.LogInCallback;
//import com.parse.ParseCloud;
//import com.parse.ParseException;
//import com.parse.ParseFacebookUtils;
//import com.parse.ParseInstallation;
//import com.parse.ParseObject;
//import com.parse.ParseUser;
//import com.parse.SaveCallback;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.mycheez.R;
import com.mycheez.application.MyCheezApplication;
import com.mycheez.enums.UpdateType;


public class LoginActivity extends Activity {

    private TextView loadingText;
    private Button loginFBButton;
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

        loginFBButton = (Button) findViewById(R.id.loginButton);
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
                    Animation animFade  = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
                    loginFBButton.startAnimation(animFade);
//                    loginFBButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            hideLoginButton();
//                            String signinMsg = getResources().getString(R.string.signing_in_message);
//                            showLoadingMsgSection(signinMsg);
//                            loginToFBAndCreateUser();
//                        }
//                    });
                }


                //checkNetworkAvailability();
//                ParseUser currentUser = ParseUser.getCurrentUser();
//
//                if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
//                    // user exists
//                    String stealZoneMsg = getResources().getString(R.string.prep_steal_zone_message);
//                    showLoadingMsgSection(stealZoneMsg);
//                    performCreateAndLogin(false);
//
//                } else {
//                    // user does not exists
//                    loginFBButton.setVisibility(View.VISIBLE);
//                    Animation animFade  = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
//                    loginFBButton.startAnimation(animFade);
//                    loginFBButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            hideLoginButton();
//                            String signinMsg = getResources().getString(R.string.signing_in_message);
//                            showLoadingMsgSection(signinMsg);
//                            loginToFBAndCreateUser();
//                        }
//                    });
//                }
            }
        });
        LinearLayout titleContainer = (LinearLayout) findViewById(R.id.titleContainer);
        titleContainer.startAnimation(animTranslate);
    }

    private void initialize(){
        // initialize Firebase reference
        firebaseRef = new Firebase("https://torrid-inferno-8611.firebaseio.com");
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
                LoginActivity.this.onFacebookAccessTokenChange(currentAccessToken);
            }
        };
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
        }
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
            loginFBButton.setVisibility(View.GONE);
            // START THEFT ACTIVITY HERE!!
            startTheftActivity();

        } else {
            /* No authenticated user show all the login buttons */
            loginFBButton.setVisibility(View.VISIBLE);
        }
        this.mAuthData = authData;
        /* invalidate options menu to hide/show the logout button */
        //supportInvalidateOptionsMenu();
    }

    private void showLoadingMsgSection(String message)
    {
        loadingMsgSection.setVisibility(View.VISIBLE);
        Animation animFade  = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
        loadingMsgSection.startAnimation(animFade);
        loadingText.setText(message);
    }

    private void hideLoadingMsgSection()
    {
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

    private void loginToFBAndCreateUser() {

        List<String> permissions = Arrays.asList("public_profile", "user_friends");
//        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
//            @Override
//            public void done(ParseUser user, ParseException err) {
//                if(err != null){
//                    Log.e(StealTheCheeseApplication.LOG_TAG, "Error in creating new user", err);
//                    hideLoadingMsgSection();
//                    showLoginButton();
//                }
//                if (user == null) {
//                    Log.i(StealTheCheeseApplication.LOG_TAG, "Uh oh. The user cancelled the Facebook login.");
//                } else if (user.isNew()) {
//                    Log.i(StealTheCheeseApplication.LOG_TAG, "User signed up and logged in through Facebook!");
//                    getFBUserInfo();
//                } else {
//                    Log.i(StealTheCheeseApplication.LOG_TAG, "User logged in through Facebook!");
//                    performCreateAndLogin(false);
//                }
//            }
//        });

    }

    private void showLoginButton()
    {
        ViewGroup parentView = (ViewGroup) loginFBButton.getParent();
        parentView.setVisibility(View.VISIBLE);
    }

    private void hideLoginButton()
    {
        ViewGroup parentView = (ViewGroup) loginFBButton.getParent();
        parentView.setVisibility(View.GONE);
    }

    private void performCreateAndLogin(boolean isNewUser){
        final Map<String,Object> params = new HashMap<String,Object>();
        params.put("isNewUserFlag", isNewUser);

//        ParseCloud.callFunctionInBackground("onLoginActivity", params, new FunctionCallback<List<ParseUser>>() {
//
//            @Override
//            public void done(List<ParseUser>allUsersData, ParseException ex) {
//                if (ex == null){
//                    ParseUser.pinAllInBackground(StealTheCheeseApplication.PIN_TAG, allUsersData, new SaveCallback() {
//
//                        @Override
//                        public void done(ParseException ex) {
//                            if(ex == null){
//                                ParseUser curr = ParseUser.getCurrentUser();
//                                String fbId = (String)curr.get("facebookId");
//                                ParseInstallation.getCurrentInstallation().put("facebookId", fbId);
//                                ParseInstallation.getCurrentInstallation().saveInBackground();
//                                updateCheeseCountData();
//                            }else {
//                                Log.e(StealTheCheeseApplication.LOG_TAG, "Error pinning", ex);
//                            }
//
//                        }
//                    });
//
//                }else {
//                    Log.e(StealTheCheeseApplication.LOG_TAG, "Error fetching data from cloud code: " , ex);
//                    Toast loginFailedToast = Toast.makeText(getApplicationContext(), R.string.login_failed_message, Toast.LENGTH_LONG);
//                    loginFailedToast.setGravity(Gravity.CENTER, 0, 0);
//                    loginFailedToast.show();
//                }
//            }
//        });
    }

    private void getFBUserInfo() {
//        loadingText.setText("Getting user profile info...");
//        final ParseUser loggedInUser = ParseUser.getCurrentUser();
//        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
//            @Override
//            public void onCompleted(GraphUser user, Response response) {
//                loggedInUser.put("facebookId", user.getId());
//                loggedInUser.put("firstName", user.getFirstName());
//                loggedInUser.put("lastName", user.getLastName());
//                // Use ProfilePictureView if needed for UI
//                loggedInUser.put("profilePicUrl", String.format(StealTheCheeseApplication.PROFILE_PIC_URL, user.getId()));
//                loggedInUser.saveInBackground(new SaveCallback() {
//
//                    @Override
//                    public void done(ParseException parseexception) {
//                        performCreateAndLogin(true);
//                    }
//                });
//            }
//        });
//        request.executeAsync();

    }


    private void updateCheeseCountData(){
        final Map<String,Object> params = new HashMap<String,Object>();
//        ParseCloud.callFunctionInBackground("getAllCheeseCounts", params, new FunctionCallback<HashMap<String, Object>>() {
//
//            @Override
//            public void done(HashMap<String, Object> wrapper, ParseException ex) {
//                if(ex == null){
//                    if(wrapper.containsKey("countDown")){
//                        timeLeft = (Double)wrapper.get("countDown");
//                        timeLeft +=100; //buffer time
//                    }
//
//                    List<HashMap<String, Object>> cheeseCounts = (List<HashMap<String, Object>>)wrapper.get("cheeseCountList");
//                    List<ParseObject> allCountList = new ArrayList<ParseObject>();
//                    for(HashMap<String, Object> eachCount : cheeseCounts){
//
//                        String friendFacebookId = (String)eachCount.get("facebookId");
//                        int cheeseCount = (Integer)eachCount.get("cheeseCount");
//                        boolean showMe = (Boolean)eachCount.get("showMe");
//                        boolean animateMe = (Boolean)eachCount.get("animateMe");
//
//                        ParseObject tempObject = new ParseObject("cheeseCountObj");
//                        tempObject.put("facebookId", friendFacebookId);
//                        tempObject.put("cheeseCount", cheeseCount);
//                        tempObject.put("showMe", showMe);
//                        tempObject.put("animateMe", animateMe);
//
//                        allCountList.add(tempObject);
//                    }
//
//                    ParseObject.pinAllInBackground(StealTheCheeseApplication.PIN_TAG, allCountList, new SaveCallback() {
//                        @Override
//                        public void done(ParseException ex) {
//                            startTheftActivity();
//
//                        }
//                    });
//                }
//            }
//        });
    }



    /**
     * TODO : FIX ME !!!
     */
    private void startTheftActivity() {
//        Intent intent = new Intent(LoginActivity.this, TheftActivity.class);
//        // removing this activity from backstack
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        String updateType = getResources().getString(R.string.update_type);
//        intent.putExtra(updateType, UpdateType.LOGIN);
//        intent.putExtra("CountDown", timeLeft);
//        startActivity(intent);
//        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

}

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
import com.mycheez.util.AuthenticationHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    /* Data from the authenticated user */
    private AuthenticationHandler authHandler;
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
        authHandler.triggerOnActivityResult(requestCode, resultCode, data);
    }

    private void initialize() {
        // initialize layouts
        initializeUIComponents();
        mFirebaseRef = MyCheezApplication.getRootFirebaseRef();
        doLoginAnimation();
        authHandler = new AuthenticationHandler();
        authHandler.initialize(new AuthenticationHandler.FacebookAuthenticationValidated() {
            @Override
            public void facebookAuthenticationValidated(Boolean isValid) {
                if (isValid) {
                    loginFBButton.setVisibility(View.GONE);
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed_message), Toast.LENGTH_LONG).show();
                }
            }
        }, new AuthenticationHandler.FirebaseAuthenticationValidated() {
            @Override
            public void firebaseAuthenticationValidated(Boolean isValid, AuthData authData) {
                if (isValid) {
                    setAuthenticatedUser(authData);
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed_message), Toast.LENGTH_LONG).show();
                }
            }
        });
        //initializeFacebookLogin();
    }

    private void initializeUIComponents() {
        loginFBButton = (LoginButton) findViewById(R.id.loginButton);
        loginFBButton.setVisibility(View.GONE);
        loginFBButton.setReadPermissions(Arrays.asList("public_profile", "user_friends"));
        loadingMsgSection = (LinearLayout) findViewById(R.id.loadingMsgSection);
        loadingMsgSection.setVisibility(View.GONE);
        loadingText = (TextView) findViewById(R.id.loadingText);
        titleContainer = (LinearLayout) findViewById(R.id.titleContainer);
    }


    private void doLoginAnimation() {
        Animation animTranslate = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.translate);
        animTranslate.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                authHandler.validateUserAuthentication(new AuthenticationHandler.UserAuthenticationValidated() {
                    @Override
                    public void userAuthenticationValidated(Boolean isValid) {
                        if (isValid) {
                            AuthData authData = mFirebaseRef.getAuth();
                            setAuthenticatedUser(authData);
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
            }
        });
        titleContainer.startAnimation(animTranslate);
    }

    /**
     * Once a user is logged in, take the mAuthData provided from Firebase and "use" it.
     * 1. Populates basic profile info of user
     * 2. Call Facebook graph api to get friends list
     * 3. Upsert current user in Firebase
     */
    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            showLoadingMsgSection(getString(R.string.prep_steal_zone_message));
            populateProfileInfoForUser(authData);
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
                            hideLoadingMsgSection();
                            if (isSuccess) {
                                // Set the user object in Appplication scope
                                MyCheezApplication.setCurrentUser(currentUser);
                                FirebaseProxy.setupUserPresence(currentUser);
                                startTheftActivity();
                            } else {
                                Log.e(TAG, "Error upserting user data");
                                Toast.makeText(LoginActivity.this, getString(R.string.upsert_failed_message), Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }
            });
            batch.executeAsync();
        }

    }

    /**
     * Facebook graph api call to get Friends list of current user
     *
     * @return
     */
    private GraphRequest generateFriendListRequest() {
        return GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray jsonArray, GraphResponse response) {
                        List<String> list = new ArrayList<>();
                        try {
                            for (int i = 0; i < jsonArray.length(); i++) {
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
    private void populateProfileInfoForUser(AuthData authData) {
        currentUser.setFacebookId((String) authData.getProviderData().get("id"));
        Map<String, Object> userProfileData = (Map) authData.getProviderData().get("cachedUserProfile");
        currentUser.setFirstName((String) userProfileData.get("first_name"));
        currentUser.setLastName((String) userProfileData.get(("last_name")));
        Map<String, Object> pictureData = (Map) userProfileData.get(("picture"));
        currentUser.setProfilePicUrl((String) ((Map) pictureData.get("data")).get("url"));
    }

    private void showLoadingMsgSection(String message) {
        loadingMsgSection.setVisibility(View.VISIBLE);
        Animation animFade = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
        loadingMsgSection.startAnimation(animFade);
        loadingText.setText(message);
    }

    private void hideLoadingMsgSection() {
        loadingMsgSection.setVisibility(View.GONE);
    }

    private void checkNetworkAvailability() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            Toast.makeText(this, R.string.no_network_message, Toast.LENGTH_LONG).show();
            startTheftActivity();
        }
    }

    private void startTheftActivity() {
        Intent intent = new Intent(LoginActivity.this, TheftActivity.class);
        intent.putExtra("facebookId", currentUser.getFacebookId());
        startActivity(intent);
        finish();
    }


}

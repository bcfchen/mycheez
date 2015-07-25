package com.mycheez.util;

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.mycheez.application.MyCheezApplication;

import org.json.JSONObject;

/* class that handles facebook/firebase authentication */
public class AuthenticationHandler {
    private static Firebase mFirebaseRef = MyCheezApplication.getMyCheezFirebaseRef();

    /* call firebase and facebook to see if credentials are still valid, or have expired */
    public static void validateUserAuthentication(final UserAuthenticationValidated callback){
        AuthData firebaseAuthData = mFirebaseRef.getAuth();

        // declare user credentials invalid if firebase creds not valid
        if (firebaseAuthData == null){
            callback.userAuthenticationValidated(false);
            return;
        }

        // now check with facebook given existing access token
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // invoke callback with true/false based on authentication response from facebook
                        if (response.getError() == null){
                            callback.userAuthenticationValidated(true);
                        } else {
                            callback.userAuthenticationValidated(false);
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public interface UserAuthenticationValidated{
        void userAuthenticationValidated(Boolean isValid);
    }


}

package com.mycheez.factories;

import com.firebase.client.AuthData;
import com.mycheez.model.User;

public class UserFactory {
    public static final User createNewUser(AuthData authData){
        return new User();
    }
}

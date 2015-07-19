package com.mycheez.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationHelper {
    private String provider;
    private String id;

    public AuthenticationHelper(String uid){
        this.provider = uidToProvider(uid);
        this.id = uidToId(uid);
    }

    private String uidToProvider(String uid){
        return uid.substring(0, uid.indexOf(":"));
    }

    private String uidToId(String uid){
        return "10104704492062016";
        //return uid.substring(uid.indexOf(":"), uid.length());
    }


}

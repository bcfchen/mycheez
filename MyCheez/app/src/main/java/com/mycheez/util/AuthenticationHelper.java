package com.mycheez.util;

public class AuthenticationHelper {
    private String provider;
    private String id;

    public AuthenticationHelper(String uid){
        this.provider = uidToProvider(uid);
        this.id = uidToId(uid);
    }

    private String uidToProvider(String uid){
        return uid.substring(0, uid.indexOf(";")-1);
    }

    private String uidToId(String uid){
        return uid.substring(uid.indexOf(":"), uid.length()-1);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}

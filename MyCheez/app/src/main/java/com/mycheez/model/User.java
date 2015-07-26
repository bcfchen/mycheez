package com.mycheez.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.ServerValue;
import com.mycheez.application.MyCheezApplication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties
@ToString(includeFieldNames=true)
public class User {
    private String facebookId;
    private int cheeseCount = 50;   // default cheese count
    private Date createdAt;
    private String firstName;
    private List<String> friends = new ArrayList<>();
    private String lastName;
    private String profilePicUrl;
    private Long updatedAt;
    private Boolean isOnline;


    public java.util.Map<String, String> getUpdatedAt() {
        return ServerValue.TIMESTAMP;
    }

    @JsonIgnore
    public Long getUpdatedAtLong() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProfilePicUrl(){
        return String.format(MyCheezApplication.PROFILE_PIC_URL, this.facebookId);
    }

}

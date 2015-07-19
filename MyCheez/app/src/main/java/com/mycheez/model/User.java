package com.mycheez.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties
public class User{
    private String facebookId;
    private int cheeseCount;
    private Date createdAt;
    private String firstName;
    private String[] friends;
    private Boolean isOnline;
    private String lastName;
    private String profilePicUrl;
    private Date updatedAt;
}

package com.mycheez.model;

import java.net.URI;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
//@NoArgsConstructor
//@AllArgsConstructor
public class User{
    private String facebookId;
    private int cheeseCount;
    private Date createdAt;
    private String fName;
    private String[] friends;
    private Boolean isOnline;
    private String lName;
    private URI profilePicUrl;
    private Date updatedAt;
}

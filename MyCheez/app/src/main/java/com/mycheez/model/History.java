package com.mycheez.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties
@ToString(includeFieldNames=true)
public class History {
    private String thiefName;
    private String thiefId;

}

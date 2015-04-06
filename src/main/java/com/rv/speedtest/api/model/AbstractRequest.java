package com.rv.speedtest.api.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

@Data
@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "type")  
@JsonSubTypes({  
    @Type(value = LaunchRequest.class, name = "LaunchRequest"),  
    @Type(value = IntentRequest.class, name = "IntentRequest"),
    @Type(value = SessionEndedRequest.class, name = "SessionEndedRequest")})
public abstract class AbstractRequest
{
    private String type;
    private String requestId;
}

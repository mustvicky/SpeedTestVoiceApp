package com.rv.speedtest.api.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Session
{
    @JsonProperty("new")
    private boolean isNew;
    private String sessionId;
    private Map<String, Object> attributes;
    private User user;
}

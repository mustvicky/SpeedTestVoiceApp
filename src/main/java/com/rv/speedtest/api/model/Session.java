package com.rv.speedtest.api.model;

import java.util.Map;

import lombok.Data;

@Data
public class Session
{
    private String sessionId;
    private Map<String, Object> attributes;
    private User user;
}

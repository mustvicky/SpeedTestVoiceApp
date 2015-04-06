package com.rv.speedtest.api.model;

import lombok.Data;

@Data
public class SessionEndedRequest
{
    private String type;
    private String requestId;
    private String reason;
}

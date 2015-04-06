package com.rv.speedtest.api.model;

import lombok.Data;

@Data
public class IntentRequest
{
    private String type;
    private String requestId;
    private Intent intent;
}

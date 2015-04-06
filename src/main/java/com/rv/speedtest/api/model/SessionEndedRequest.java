package com.rv.speedtest.api.model;

import lombok.Data;

@Data
public class SessionEndedRequest extends AbstractRequest
{
    private String reason;
}

package com.rv.speedtest.api.model;

import lombok.Data;

@Data
public class SpeechletRequest
{
    private String version;
    private Session session;
    private AbstractRequest request;
    private boolean doNotCheckSignature;
}

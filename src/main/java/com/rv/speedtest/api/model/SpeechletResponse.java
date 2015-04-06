package com.rv.speedtest.api.model;

import java.util.Map;

import lombok.Data;

@Data
public class SpeechletResponse
{
    private String version = "1.0";
    private Map<String, Object> sessionAttributes;
    private Response response;
}

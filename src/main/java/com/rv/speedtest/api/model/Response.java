package com.rv.speedtest.api.model;

import lombok.Data;

@Data
public class Response
{
    private OutputSpeech outputSpeech;
    private Card card;
    boolean shouldEndSession;
}

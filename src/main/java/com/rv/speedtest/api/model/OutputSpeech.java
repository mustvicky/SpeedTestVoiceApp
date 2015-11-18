package com.rv.speedtest.api.model;

import lombok.Data;

@Data
public class OutputSpeech
{
    private String type = "SSML";
    private String ssml;
}

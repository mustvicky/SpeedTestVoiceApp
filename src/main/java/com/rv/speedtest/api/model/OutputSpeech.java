package com.rv.speedtest.api.model;

import lombok.Data;

@Data
public class OutputSpeech
{
    private String type = "PlainText";
    private String text;
}

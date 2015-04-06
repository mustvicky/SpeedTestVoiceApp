package com.rv.speedtest.api.model;

import lombok.Data;

@Data
public class Card
{
    private String type = "Simple";
    private String title;
    private String subtitle;
    private String content;
}

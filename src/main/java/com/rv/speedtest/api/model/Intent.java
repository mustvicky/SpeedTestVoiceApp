package com.rv.speedtest.api.model;

import java.util.Map;

import lombok.Data;

@Data
public class Intent
{
    private String name;
    private Map<String, Slot> slots;
}

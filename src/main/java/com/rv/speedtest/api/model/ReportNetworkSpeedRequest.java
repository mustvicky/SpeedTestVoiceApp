package com.rv.speedtest.api.model;

import lombok.Data;

@Data
public class ReportNetworkSpeedRequest
{
    private String messageId;
    private String networkSpeedInKb;
}

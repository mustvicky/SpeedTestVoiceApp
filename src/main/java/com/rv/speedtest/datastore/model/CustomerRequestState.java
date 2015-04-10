package com.rv.speedtest.datastore.model;

import lombok.Data;

@Data
public class CustomerRequestState
{
    private String customerRequestId;
    private CustomerState customerState;
    /**
     * The absolute time after which this request is deemed expired
     */
    private long requestExpiryTimeMillis;
    private NetworkSpeedRequest networkSpeedRequest; 
    private NetworkSpeedResponse networkSpeedResponse;
}

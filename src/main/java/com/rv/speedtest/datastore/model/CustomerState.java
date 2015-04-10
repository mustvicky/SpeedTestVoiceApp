package com.rv.speedtest.datastore.model;

import lombok.Data;

@Data
public class CustomerState
{
    private String userId;
    /**
     * The absolute time in millis when an invitation code sent to the user will be abandoned
     */
    private long invitationExpiryTimeMillis;
    private String invitationCode;
    private String mobileRegistrationId;
}

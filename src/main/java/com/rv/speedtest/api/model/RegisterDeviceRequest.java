package com.rv.speedtest.api.model;

import lombok.Data;

@Data
public class RegisterDeviceRequest
{
    String mobileRegistrationId;
    String invitationCode;
}

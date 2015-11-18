package com.rv.speedtest.datastore.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DynamoDBTable(tableName = "CustomerState")
@NoArgsConstructor
public class CustomerState
{
    @DynamoDBHashKey(attributeName="UserId")
    private String userId;

    @DynamoDBAttribute(attributeName="InvitationCodeExpiryTimeUTC")
    private String invitationCodeExpiryTimeStringUTC;
    
    @DynamoDBAttribute(attributeName="RecordCreationTimeUTC")
    private String recordCreationTimeUTC;
    
    @DynamoDBIndexHashKey(attributeName = "InvitationCode", globalSecondaryIndexName = "InvitationCode-index")
    private String invitationCode;
    
    @DynamoDBAttribute(attributeName="DeviceType")
    private String deviceType;
    
    @DynamoDBAttribute(attributeName="DeviceRegistrationId")
    private String deviceRegistrationId;
}

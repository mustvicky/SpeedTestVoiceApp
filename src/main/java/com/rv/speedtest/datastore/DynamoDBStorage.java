package com.rv.speedtest.datastore;

import java.util.List;
import java.util.Random;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator;
import com.rv.speedtest.api.DateUtils;
import com.rv.speedtest.api.model.InvitationCode;
import com.rv.speedtest.datastore.model.CustomerState;

@CommonsLog
public class DynamoDBStorage implements Storage
{
    private final DynamoDBMapper mapper;
    
    public DynamoDBStorage(DynamoDBMapper mapper)
    {
        this.mapper = mapper;
    }
    
    private int INVITATION_CODE_DIGITS = 4;
    
    @Override
    public CustomerState getCustomerStateFromUserId(String userId)
    {
        CustomerState customerState = new CustomerState();
        customerState.setUserId(userId);
        CustomerState existingCustomerState = mapper.load(customerState);
        return existingCustomerState;
    }

    @Override
    public CustomerState getCustomerStateFromInviteCode(InvitationCode invitationCode)
    {
        Validate.notNull(invitationCode, "Invitation code object cannot be null");
        Validate.notNull(invitationCode.getCode(), "Invitation code cannot be null");
        
        CustomerState customerState = null;
        CustomerState invitationKey = new CustomerState();
        invitationKey.setInvitationCode(invitationCode.getCode());
        
        DateTime now = new DateTime();
        Condition withinExpiryTimeCondition = new Condition()
            .withComparisonOperator(ComparisonOperator.LT.toString())
            .withAttributeValueList(new AttributeValue().withS(DateUtils.toDateString(now.toDate())));
        
        DynamoDBQueryExpression<CustomerState> queryExpression = new DynamoDBQueryExpression<CustomerState>()
                .withConsistentRead(false)
                .withHashKeyValues(invitationKey)
                .withQueryFilterEntry("InvitationCodeExpiryTimeUTC", withinExpiryTimeCondition);
        
        List<CustomerState> customerStates = mapper.query(CustomerState.class, queryExpression);
        if (customerStates != null && !customerStates.isEmpty())
        {
            log.info("found customer state with invitation code = [" + invitationCode.getCode() + "]");
            if (customerStates.size() > 0)
            {
                log.info("Invalid state, more than 1 object for the invitation code = [" + invitationCode.getCode() + "]. Returning null");
                return null;
            }
            else
            {
                customerState = customerStates.get(0);
            }
        }
        return customerState;
    }

    @Override
    public boolean invalidateCustomerStateFromUsedId(String userId)
    {
        CustomerState customerState = new CustomerState();
        customerState.setUserId(userId);
        CustomerState existingCustomerState = mapper.load(customerState);
        mapper.delete(customerState);
        if (existingCustomerState != null)
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean invalidateCustomerStateFromInvitationCode(String inviteCode)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean invalidateCustomerRequest(String requestId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean saveCustomerState(CustomerState customerState)
    {
        mapper.save(customerState);
        return true;
    }

    @Override
    public InvitationCode getUniqueInvitationCode()
    {
        InvitationCode invitationCode = null;
        do
        {
            invitationCode = generateRandomInvitationCode();
            log.info("Random invitation code = [" + invitationCode.getCode() + "]");
            CustomerState customerState = getCustomerStateFromInviteCode(invitationCode);
            if (customerState != null)
            {
                log.info("Collission detected for customer state = [" + invitationCode.getCode() + "] with user id = [" + customerState.getUserId() + "]");
                invitationCode = null;
            }
        }
        while (invitationCode == null);
        log.info("Generating invitation code = [" + invitationCode.getCode() + "]");
        return invitationCode;
    }

    private InvitationCode generateRandomInvitationCode()
    {
        InvitationCode ic = new InvitationCode();
        int randomNumber = Math.abs((new Random().nextInt() % ((int)Math.pow(10, INVITATION_CODE_DIGITS))));

        StringBuilder randomCode = toInvitationCodeString(randomNumber);
        ic.setCode(randomCode.toString());
        return ic;
    }

    private StringBuilder toInvitationCodeString(int randomNumber)
    {
        StringBuilder randomCode = new StringBuilder();
        int i = 0;
        while (i < INVITATION_CODE_DIGITS)
        {
            i++;
            if (randomNumber == 0)
            {
                randomCode.insert(0, " ");
            }
            else
            {
                int lastDigit = randomNumber % 10;
                randomNumber /= 10;
                randomCode.insert(0, lastDigit);
            }
        }
        return randomCode;
    }
    
}

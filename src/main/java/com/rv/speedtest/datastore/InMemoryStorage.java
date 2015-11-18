package com.rv.speedtest.datastore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.rv.speedtest.api.model.InvitationCode;
import com.rv.speedtest.datastore.model.CustomerState;

public class InMemoryStorage /*implements Storage*/
{
    /*
    private Map<String, CustomerState> customerToCustomerState = new HashMap<String, CustomerState>();
    private Map<String, CustomerState> inviteCodeToCustomerState = new HashMap<String, CustomerState>();

    @Override
    public synchronized CustomerState getCustomerStateFromUserId(String userId)
    {
        return customerToCustomerState.get(userId);
    }
    
    @Override
    public CustomerState getCustomerStateFromInviteCode(String inviteCode)
    {
        return inviteCodeToCustomerState.get(inviteCode);
    }

    @Override
    public synchronized boolean invalidateCustomerStateFromUsedId(String userId)
    {
        CustomerState customerState = customerToCustomerState.remove(userId);
        if (customerState != null)
        {
            inviteCodeToCustomerState.remove(customerState.getInvitationCode());
        }
        return customerState != null;
    }
    
    @Override
    public synchronized boolean invalidateCustomerStateFromInvitationCode(String inviteCode)
    {
        CustomerState customerState = inviteCodeToCustomerState.remove(inviteCode);
        if (customerState != null)
        {
            customerToCustomerState.remove(customerState.getUserId());
        }
        return customerState != null;
    }

    @Override
    public synchronized boolean saveCustomerState(CustomerState customerState)
    {
//        CustomerState old = customerToCustomerState.get(customerState.getUserId());
//        if (old != null)
//        {
//            throw new CustomerStateAlreadyExists("Customer [" + customerState.getUserId() + "] already exists");
//        }
//        
//        old = inviteCodeToCustomerState.get(customerState.getInvitationCode());
//        
//        if (old != null)
//        {
//            throw new CustomerInviteCodeAlreadyExists("Customer invite code [" + customerState.getInvitationCode() + "] already exists");
//        }
        
        customerToCustomerState.put(customerState.getUserId(), customerState);
        inviteCodeToCustomerState.put(customerState.getInvitationCode(), customerState);
        return true;
    }


    @Override
    public InvitationCode getUniqueInvitationCode()
    {
        return generateRandomInvitationCode();
    }
    
    private InvitationCode generateRandomInvitationCode()
    {
        InvitationCode ic = new InvitationCode();
        int randomNumber = Math.abs((new Random().nextInt() % 10000));
        ic.setGuiCode(randomNumber + "");
        StringBuilder randomCode = new StringBuilder();
        while (randomNumber > 0)
        {
            int lastDigit = randomNumber % 10;
            randomNumber /= 10;
            randomCode.insert(0, " " + lastDigit);
        }
        ic.setVuiCode(randomCode.substring(1, randomCode.length()));
        return ic;
    }

    @Override
    public boolean invalidateCustomerRequest(String requestId)
    {
        // TODO Auto-generated method stub
        return false;
    }
*/
}

package com.rv.speedtest.datastore;

import java.util.HashMap;
import java.util.Map;

import com.rv.speedtest.datastore.model.CustomerRequestState;
import com.rv.speedtest.datastore.model.CustomerState;

public class InMemoryStorage implements Storage
{
    private Map<String, CustomerState> customerToCustomerState = new HashMap<String, CustomerState>();
    private Map<String, CustomerState> inviteCodeToCustomerState = new HashMap<String, CustomerState>();
    private Map<String, CustomerRequestState> customerToRequestState = new HashMap<String, CustomerRequestState>();
    private Map<String, CustomerRequestState> requestToRequestState = new HashMap<String, CustomerRequestState>();

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
    public synchronized boolean createCustomerState(CustomerState customerState)
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
    public synchronized CustomerRequestState getCustomerRequestState(CustomerState customerState)
    {
        return customerToRequestState.get(customerState.getUserId());
    }
    
    @Override
    public synchronized boolean invalidateCustomerRequest(String requestId)
    {
        CustomerRequestState requestState = requestToRequestState.remove(requestId);
        if (requestState != null)
        {
            customerToRequestState.remove(requestState.getCustomerState().getUserId());
            return true;
        }
        return false;
    }

    @Override
    public boolean createCustomerRequestState(CustomerRequestState customerRequestState)
    {
//        CustomerRequestState old = customerToRequestState.get(customerRequestState.getCustomerState().getUserId());
//        if (old != null)
//        {
//            throw new CustomerRequestAlreadyExists("Customer [" + customerRequestState.getCustomerState().getUserId() + "] request already exists");
//        }
//        
//        old = requestToRequestState.get(customerRequestState.getCustomerRequestId());
//        if (old != null)
//        {
//            throw new CustomerRequestAlreadyExists("Customer request [" + customerRequestState.getCustomerRequestId() + "] already exists");
//        }
        requestToRequestState.put(customerRequestState.getCustomerRequestId(), customerRequestState);
        return true;
    }

}

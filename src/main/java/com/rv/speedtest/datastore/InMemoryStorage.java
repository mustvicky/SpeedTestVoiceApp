package com.rv.speedtest.datastore;

import java.util.HashMap;
import java.util.Map;

import com.rv.speedtest.datastore.model.CustomerRequestState;
import com.rv.speedtest.datastore.model.CustomerState;

public class InMemoryStorage implements Storage
{
    private Map<String, CustomerState> customerToCustomerState = new HashMap<String, CustomerState>();
    private Map<String, CustomerRequestState> customerToRequestState = new HashMap<String, CustomerRequestState>();

    @Override
    public synchronized CustomerState getCustomerState(String userId)
    {
        return customerToCustomerState.get(userId);
    }

    @Override
    public synchronized boolean invalidateCustomerState(String userId)
    {
        CustomerState customerState = customerToCustomerState.remove(userId);
        return customerState != null;
    }

    @Override
    public synchronized boolean createCustomerState(CustomerState customerState)
    {
        CustomerState old = customerToCustomerState.get(customerState.getUserId());
        if (old != null)
        {
            throw new CustomerStateAlreadyExists("Customer [" + customerState.getUserId() + "] already");
        }
        customerToCustomerState.put(customerState.getUserId(), customerState);
        return true;
    }

    @Override
    public CustomerRequestState getCustomerRequestState(CustomerState customerState)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean createCustomerRequestState(CustomerRequestState customerRequestState)
    {
        // TODO Auto-generated method stub
        return false;
    }

}

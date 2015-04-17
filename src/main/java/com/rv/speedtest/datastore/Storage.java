package com.rv.speedtest.datastore;

import com.rv.speedtest.datastore.model.CustomerRequestState;
import com.rv.speedtest.datastore.model.CustomerState;

public interface Storage
{
    /**
     * Returns the customer state present with the system. Returns null if nothing is present
     * @param userId
     * @return
     */
    CustomerState getCustomerStateFromUserId(String userId);
    CustomerState getCustomerStateFromInviteCode(String inviteCode);
    
    /**
     * Deletes/Removes the customer state in the system. 
     * Returns true if it was successful, otherwise false
     * @param userId
     * @return
     */
    boolean invalidateCustomerStateFromUsedId(String userId);
    boolean invalidateCustomerStateFromInvitationCode(String inviteCode);
    boolean invalidateCustomerRequest(String requestId);
    
    boolean createCustomerState(CustomerState customerState);
    
    /**
     * Given a customer state, find the CustomerRequestState from the system. 
     * If there is no outstanding request state, return null
     * @param customerState
     * @return
     */
    CustomerRequestState getCustomerRequestState(CustomerState customerState);
    CustomerRequestState getCustomerRequestState(String requestId);
    
    boolean createCustomerRequestState(CustomerRequestState customerRequestState);
}

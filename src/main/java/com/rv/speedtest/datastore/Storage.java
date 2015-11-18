package com.rv.speedtest.datastore;

import com.rv.speedtest.api.model.InvitationCode;
import com.rv.speedtest.datastore.model.CustomerState;

public interface Storage
{
    /**
     * Returns the customer state present with the system. Returns null if nothing is present
     * @param userId
     * @return
     */
    CustomerState getCustomerStateFromUserId(String userId);
    CustomerState getCustomerStateFromInviteCode(InvitationCode inviteCode);
    
    /**
     * Deletes/Removes the customer state in the system. 
     * Returns true if it was successful, otherwise false
     * @param userId
     * @return
     */
    boolean invalidateCustomerStateFromUsedId(String userId);
    boolean invalidateCustomerStateFromInvitationCode(String inviteCode);
    boolean invalidateCustomerRequest(String requestId);
    
    boolean saveCustomerState(CustomerState customerState);
    
    /**
     * Returns a unique invitation code by making sure it does not collide with any existing ones which are not yet expired
     * @return
     */
    InvitationCode getUniqueInvitationCode();
}

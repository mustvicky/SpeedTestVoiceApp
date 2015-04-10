package com.rv.speedtest.datastore;

public class CustomerStateAlreadyExists extends RuntimeException
{

    public CustomerStateAlreadyExists(String errMsg)
    {
        super(errMsg);
    }
    
}

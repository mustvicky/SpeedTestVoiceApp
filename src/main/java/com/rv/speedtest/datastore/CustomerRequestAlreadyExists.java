package com.rv.speedtest.datastore;

public class CustomerRequestAlreadyExists extends RuntimeException
{

    public CustomerRequestAlreadyExists(String errMsg)
    {
        super(errMsg);
    }

}

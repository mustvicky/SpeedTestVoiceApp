package com.rv.speedtest.api;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.DateTime;

public class DateUtils
{
    private DateUtils(){}
    
    private static FastDateFormat DATE_FORMATTER = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("UTC"));
    
    public static String toDateString(Date date)
    {
        return DATE_FORMATTER.format(date);
    }
    
    public static Date getInvitationCodeExpiryTimeFromNow()
    {
        DateTime now = new DateTime();
        DateTime fiveMinsAhead = now.plusMinutes(5);
        return fiveMinsAhead.toDate();
    }
}

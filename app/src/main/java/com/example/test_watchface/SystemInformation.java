package com.example.test_watchface;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SystemInformation {
    String getTimeStamp()        // Puts the system time acquired into the desired format wanted.
    {
        DateFormat datetimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US);      // Specified format of the time, in US style.
        Date current = new Date();      // Calls the current date from the system.
        return datetimeFormat.format(current);  // Returns the date and time the system is in.
    }
}

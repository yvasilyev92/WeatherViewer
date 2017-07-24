package com.deitel.weatherviewer;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * Created by Yevgeniy on 6/15/2017.
 */

public class Weather {


    public final String dayOfWeek;
    public final String minTemp;
    public final String maxTemp;
    public final String humidity;
    public final String description;
    public final String iconURL;


    //constructor
    public Weather(long timeStamp, double minTemp, double maxTemp, double humidity, String description, String iconName){

        //NumberFormat to format double temps rounded to integers
        //the NumberFormat object creates Strings from numeric values.
        //Here we configure the object to round floatingpoint values to whole numbers
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(0);


        //Here we call our utility method convertTimeStampToDay to get the String day name
        //and initialize dayOfWeek
        this.dayOfWeek = convertTimeStampToDay(timeStamp);

        //Here we format the day's min and max temp values as whole numbers using numberFormat
        //we append "degrees F" to the end of each formatted String
        this.minTemp = numberFormat.format(minTemp) + "\u00B0F";
        this.maxTemp = numberFormat.format(maxTemp) + "\u00B0F";

        //Here we get a NumberFormat for locale-specific percent formatting
        //then use it to format the humidity percentage. The web service returns this
        //number as a whole number so we divide by 100.00 for formatting.
        //ex: 1.00 is formatted as 100%, 0.50 is formatted as 50%
        this.humidity = NumberFormat.getPercentInstance().format(humidity / 100.00);

        //Here we initialize the weather condition desc.
        this.description = description;

        //Here we create a URL string representing the weather condition image for the day's weather
        //this will be used to download the image.
        this.iconURL = "http://openweathermap.org/img/w/" + iconName + ".png";




    }



    //utility method convertTimeStampToDay receives as its argument a long value
    private static String convertTimeStampToDay(long timeStamp){

        //Here we get a Calendar object for manipulating dates and times
        Calendar calendar = Calendar.getInstance();

        //Here we call setTimeInMillis method to set the time using the timeStamp argument.
        //The timestamp is in seconds so we multiply by 1000 to convert it to milliseconds.
        calendar.setTimeInMillis(timeStamp * 1000);

        //Here we get the default TimeZone object, which we use to adjust the time, based on
        //the devices timezone.
        TimeZone tz = TimeZone.getDefault(); //get devices timezone.
        //ajust time for devices timezone
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        //Here we create a SimpleDateFormat that formats a Date object.
        //The argument "EEEE" formats the Date as just the day name
        //like "Monday", "Tuesday", etc
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE");
        //Then we format and return the day name. Calendars' getTime method returns
        //a Date object containing the time. This date is passed to the SimpleDateFormats
        //format method to get the day name.
        return dateFormatter.format(calendar.getTime());
    }
}

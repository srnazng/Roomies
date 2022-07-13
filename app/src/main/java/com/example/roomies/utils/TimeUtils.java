package com.example.roomies.utils;

import java.text.DateFormatSymbols;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
    public static final String TAG = "Utils";

    /**
     *
     * @param num      month number from 0 to 11
     * @return         name of month (January etc.)
     */
    // get name of month from number
    public static String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11) {
            month = months[num];
        }
        return month;
    }

    /**
     * @param hourOfDay hour of day in military time
     * @param minute    minute in military time
     * @return          hh:mm AM or hh:mm PM
     */
    // 24 hour time to 12 hour time with AM or PM
    public static String convertFromMilitaryTime(int hourOfDay, int minute){
        return ((hourOfDay > 12) ? hourOfDay % 12 : hourOfDay) + ":" + (minute < 10 ? ("0" + minute) : minute) + " " + ((hourOfDay >= 12) ? "PM" : "AM");
    }

    /**
     * @param c     Calendar day
     * @return      Name of day of week (Sun etc.)
     */
    public static String calendarDayOfWeek(Calendar c){
        int num = c.get(Calendar.DAY_OF_WEEK);
        if(num == 1){
            return "Sun";
        }
        if(num == 2){
            return "Mon";
        }
        if(num == 3){
            return "Tues";
        }
        if(num == 4){
            return "Wed";
        }
        if(num == 5){
            return "Thurs";
        }
        if(num == 6){
            return "Fri";
        }
        return "Sat";
    }

    /**
     * Remove time (hour, minute, second, millisecond) of Calendar object
     * @param c     Calendar object
     */
    public static void clearTime(Calendar c){
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
    }

    /**
     * @param fromDate  Starting Date object
     * @param toDate    Ending Date object
     * @return          Number of days between fromDate and toDate
     */
    public static int getDaysDifference(Date fromDate, Date toDate)
    {
        if(fromDate==null||toDate==null)
            return 0;

        return (int)( (toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    /**
     * @param d1    Starting Calendar object
     * @param d2    Ending Calendar object
     * @return      Number of weeks between d1 and d2
     */
    public static long getWeeksDifference(Calendar d1, Calendar d2){

        Instant d1i = Instant.ofEpochMilli(d1.getTimeInMillis());
        Instant d2i = Instant.ofEpochMilli(d2.getTimeInMillis());

        LocalDateTime startDate = LocalDateTime.ofInstant(d1i, ZoneId.systemDefault());
        LocalDateTime endDate = LocalDateTime.ofInstant(d2i, ZoneId.systemDefault());

        return ChronoUnit.WEEKS.between(startDate, endDate);
    }

    /**
     * @param d1    Starting Calendar object
     * @param d2    Ending Calendar object
     * @return      Number of months between d1 and d2
     */
    public static long getMonthsDifference(Calendar d1, Calendar d2){

        Instant d1i = Instant.ofEpochMilli(d1.getTimeInMillis());
        Instant d2i = Instant.ofEpochMilli(d2.getTimeInMillis());

        LocalDateTime startDate = LocalDateTime.ofInstant(d1i, ZoneId.systemDefault());
        LocalDateTime endDate = LocalDateTime.ofInstant(d2i, ZoneId.systemDefault());

        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    /**
     * compare calendar objects ignoring time
     * @param c1
     * @param c2
     * @return  difference in time
     */
    public static int compare(Calendar c1, Calendar c2) {
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
            return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
        if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
            return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
        return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * determines if an event with daily recurrence occurs on a day
     * @param startRecurrence   Start of recurrence
     * @param freq              Frequency of days event occurs on
     * @param today             Date being evaluated
     * @return  whether the recurring event occurs on given date
     */
    public static boolean occursToday_dayFreq(Calendar startRecurrence, int freq, Calendar today){
        clearTime(startRecurrence);
        clearTime(today);

        int diff = getDaysDifference(startRecurrence.getTime(), today.getTime());

        if(diff % freq == 0){
            return true;
        }
        return false;
    }

    /**
     * determines if an event with weekly recurrence occurs on a day
     * @param startRecurrence   Start of recurrence
     * @param daysOfWeek        Days of week event occurs on
     * @param freq              Frequency of weeks event occurs
     * @param today             Date being evaluated
     * @return  whether the recurring event occurs on given date
     */
    public static boolean occursToday_weekFreq(Calendar startRecurrence, String daysOfWeek, int freq, Calendar today){
        clearTime(startRecurrence);
        clearTime(today);

        long diff = getWeeksDifference(startRecurrence, today);
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK) - 1;

        if(!daysOfWeek.contains(dayOfWeek + ",")){
            return false;
        }

        if(diff % freq != 0){
            return false;
        }

        return true;
    }

    /**
     * determines if an event with monthly recurrence occurs on a day
     * @param startRecurrence   Start of recurrence
     * @param freq              Frequency of months event occurs on
     * @param today             Date being evaluated
     * @return whether the recurring event occurs on given date
     */
    public static boolean occursToday_monthFreq(Calendar startRecurrence, int freq, Calendar today){
        clearTime(startRecurrence);
        clearTime(today);

        long diff = TimeUtils.getMonthsDifference(startRecurrence, today);
        if(diff % freq == 0){
            return true;
        }

        return false;
    }
}

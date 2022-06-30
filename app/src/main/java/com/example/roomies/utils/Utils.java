package com.example.roomies.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.example.roomies.AddCircleActivity;
import com.example.roomies.model.Circle;
import com.example.roomies.model.UserCircle;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utils {
    public static final String TAG = "Utils";

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

    // 24 hour time to 12 hour time with AM or PM
    public static String convertFromMilitaryTime(int hourOfDay, int minute){
        return ((hourOfDay > 12) ? hourOfDay % 12 : hourOfDay) + ":" + (minute < 10 ? ("0" + minute) : minute) + " " + ((hourOfDay >= 12) ? "PM" : "AM");
    }

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

    public static void clearTime(Calendar c){
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
    }

    public static int getDaysDifference(Date fromDate, Date toDate)
    {
        if(fromDate==null||toDate==null)
            return 0;

        return (int)( (toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    public static long getWeeksDifference(Calendar d1, Calendar d2){

        Instant d1i = Instant.ofEpochMilli(d1.getTimeInMillis());
        Instant d2i = Instant.ofEpochMilli(d2.getTimeInMillis());

        LocalDateTime startDate = LocalDateTime.ofInstant(d1i, ZoneId.systemDefault());
        LocalDateTime endDate = LocalDateTime.ofInstant(d2i, ZoneId.systemDefault());

        return ChronoUnit.WEEKS.between(startDate, endDate);
    }

    public static long getMonthsDifference(Calendar d1, Calendar d2){

        Instant d1i = Instant.ofEpochMilli(d1.getTimeInMillis());
        Instant d2i = Instant.ofEpochMilli(d2.getTimeInMillis());

        LocalDateTime startDate = LocalDateTime.ofInstant(d1i, ZoneId.systemDefault());
        LocalDateTime endDate = LocalDateTime.ofInstant(d2i, ZoneId.systemDefault());

        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

     /**
     * query UserCircle objects that contain current user to get circles that user has joined
     */

    public static void updateCirclesPreferences(Activity activity){

        List<Circle> myCircles = new ArrayList<>();
        // specify what type of data we want to query - UserCircle.class
        ParseQuery<UserCircle> query = ParseQuery.getQuery(UserCircle.class).whereEqualTo(UserCircle.KEY_USER, ParseUser.getCurrentUser());
        // include data referred by user key
        query.include(UserCircle.KEY_USER);
        query.include(UserCircle.KEY_CIRCLE);
        // start an asynchronous call for UserCircle objects that include current user
        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting userCircles", e);
                    return;
                }

                // user has not joined a circle
                if(userCircles.isEmpty()){
                    // go to AddCircleActivity
                    Intent i = new Intent(activity, AddCircleActivity.class);
                    activity.startActivity(i);
                    activity.finish();
                }

                // save received posts to list and notify adapter of new data
                myCircles.clear();
                for(UserCircle userCircle : userCircles){
                    myCircles.add(userCircle.getCircle());
                }

                JSONArray arr = new JSONArray(myCircles);

            }
        });
    }
}

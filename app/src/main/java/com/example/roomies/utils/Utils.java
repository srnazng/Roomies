package com.example.roomies.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.roomies.model.Chore;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    public static final int GET_FROM_GALLERY = 3;

    /**
     * Sign in
     * @param context
     * @param username
     * @param password
     */
    public static void loginUser(Context context, String username, String password){
        Log.i(TAG, "attempt login");

        // login in background thread
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e != null){
                    // issue
                    List<ParseUser> userList = new ArrayList<>();
                    userList.add(user);
                    ParseUser.pinAllInBackground(userList);
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(context, "Incorrect login credentials", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e(TAG, "login success");
                Session.startSession(context);
            }
        });
    }

    // format chore due date
    public static String formatDue(Chore chore, Calendar day){
        boolean isToday = (compareDates(day, Calendar.getInstance()) == 0);

        if(chore.getAllDay() && isToday){
            return "Due today";
        }

        Calendar time = Calendar.getInstance();
        time.setTime(chore.getDue());

        String due = "Due ";

        if(!chore.getAllDay()){
            String minutes = time.get(Calendar.MINUTE) + "";
            if(time.get(Calendar.MINUTE) < 10) {
                minutes = "0" + minutes;
            }

            int hour = time.get(Calendar.HOUR_OF_DAY);

            if(hour > 12){
                hour -= 12;
                due = due + hour + ":" + minutes + " PM ";
            }
            else if(hour == 12){
                due = due + hour + ":" + minutes + " PM ";
            }
            else{
                due = due + hour + ":" + minutes + " AM ";
            }
        }

        if(isToday){
            due = due + "today";
        }
        else{
            due = due + "on " + getMonthForInt(day.get(Calendar.MONTH)) + " "
                + day.get(Calendar.DAY_OF_MONTH) + ", " + day.get(Calendar.YEAR);
        }

        return due;
    }

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
        int daysDiff = getDaysDifference(d1.getTime(), d2.getTime());

        BigDecimal weeksDiff = new BigDecimal(daysDiff).divide(BigDecimal.valueOf(7), 0, RoundingMode.CEILING);
        return weeksDiff.intValue();
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
     * compareDates calendar objects ignoring time
     * @param c1
     * @param c2
     * @return  difference in time
     */
    public static int compareDates(Calendar c1, Calendar c2) {
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

        long diff = Utils.getMonthsDifference(startRecurrence, today);
        if(diff % freq == 0){
            return true;
        }

        return false;
    }

    /**
     * Convert bitmap to ParseFile
     * @param imageBitmap
     * @return
     */
    public static ParseFile conversionBitmapParseFile(Bitmap imageBitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,0,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png",imageByte);
        return parseFile;
    }

    /**
     * get file name from Uri
     * @param context
     * @param uri
     * @return
     */
    public static String getPath( Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result.substring(result.lastIndexOf("/")+1);
    }

    /**
     * Show dialog popup of image
     * @param context
     * @param imageUri
     */
    public static void showImage(Context context, Uri imageUri) {
        if(imageUri == null){
            return;
        }

        Dialog builder = new Dialog(context);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(context);
        imageView.setImageURI(imageUri);
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }
}

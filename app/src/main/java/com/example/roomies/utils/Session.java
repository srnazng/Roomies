package com.example.roomies.utils;

import static com.example.roomies.model.CircleManager.getChoreCollection;
import static com.example.roomies.model.CircleManager.getExpenseCollection;
import static com.example.roomies.utils.CalendarDayUtils.clearCalendarCache;
import static com.example.roomies.utils.CalendarDayUtils.setFirstOfMonth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;

import com.example.roomies.SplashScreenActivity;
import com.example.roomies.model.CircleManager;
import com.example.roomies.model.ScheduledNotification;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class Session {
    private Messaging messaging;
    private static Context context;
    private static Timer choreNotificationTimer;
    private static Timer expenseNotificationTimer;

    public static final String SHARED_PREF_CHORE_KEY = "choreNotificationsSet";
    public static final String SHARED_PREF_EXPENSE_KEY = "expenseNotificationsSet";

    public static final String TAG = "Session";

    public Session(Context context) {
        messaging = new Messaging(context);
        this.context = context;
        scheduleNotifications();
    }

    public static void startSession(Context context){
        Intent i = new Intent(context, SplashScreenActivity.class);
        context.startActivity(i);
    }

    public static void endSession(){
        getChoreCollection().clearAll();
        getExpenseCollection().clearAll();
        CircleManager.clearAll();
        setFirstOfMonth(null);
        clearCalendarCache();
        ParseUser.unpinAllInBackground();
    }

    public void scheduleNotifications(){
        // check of timer already set
        SharedPreferences sharedPref = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
        boolean choreNotificationsSet = sharedPref.getBoolean(SHARED_PREF_CHORE_KEY, false);
        boolean expenseNotificationsSet = sharedPref.getBoolean(SHARED_PREF_CHORE_KEY, false);

        // set notification time
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 8);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        Log.i(TAG, "ScheduleNotifications " + choreNotificationsSet + " " + expenseNotificationsSet);

        // set up chore notification
        if(!choreNotificationsSet){
            Log.i(TAG, "Set timer for chore notifications");
            choreNotificationTimer = new Timer();
            ScheduledNotification choreNotification = new ScheduledNotification(ScheduledNotification.TYPE_CHORE);
            choreNotificationTimer.schedule(choreNotification, today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(SHARED_PREF_CHORE_KEY, true);
            editor.apply();
        }

        // set up expense notification
        if(!expenseNotificationsSet){
            Log.i(TAG, "Set timer for expense notifications");
            expenseNotificationTimer = new Timer();
            ScheduledNotification expenseNotification = new ScheduledNotification(ScheduledNotification.TYPE_EXPENSE);
            expenseNotificationTimer.schedule(expenseNotification, today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(SHARED_PREF_EXPENSE_KEY, true);
            editor.apply();
        }
    }

    /**
     * Determine if connected to network
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}

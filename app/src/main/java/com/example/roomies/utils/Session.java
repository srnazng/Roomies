package com.example.roomies.utils;

import static com.example.roomies.utils.CalendarDayUtils.setFirstOfMonth;

import android.content.Context;
import android.content.Intent;

import com.example.roomies.SplashScreenActivity;

public class Session {
    private Messaging messaging;

    public Session(Context context) {
        messaging = new Messaging(context);
    }

    public static void startSession(Context context){
        Intent i = new Intent(context, SplashScreenActivity.class);
        context.startActivity(i);
    }

    public static void endSession(){
        ChoreUtils.clearAll();
        ExpenseUtils.clearAll();
        CircleUtils.clearAll();
        setFirstOfMonth(null);
    }
}
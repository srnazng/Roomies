package com.example.roomies.utils;

import android.content.Context;
import android.content.Intent;

import com.example.roomies.SplashScreenActivity;

public class Utils {
    public static void startSession(Context context){
        Intent i = new Intent(context, SplashScreenActivity.class);
        context.startActivity(i);
    }

    public static void endSession(){
        ChoreUtils.clearAll();
        ExpenseUtils.clearAll();
        CircleUtils.clearAll();
    }
}

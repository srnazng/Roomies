package com.example.roomies.utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.roomies.LoginActivity;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class UserUtils {

    public static final String TAG = "UserUtils";

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

    public static void logout(Context context){
        Messaging.clearFirebaseInstance(true);
    }

    public static void parseLogout(Context context){
        Log.i(TAG, "parse logout");
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
        Session.endSession();

        if(currentUser == null){
            Intent i = new Intent(context, LoginActivity.class);
            context.startActivity(i);
            ((Activity)context).finish();
        }
    }
}

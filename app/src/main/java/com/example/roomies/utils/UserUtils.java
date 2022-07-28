package com.example.roomies.utils;
import static com.example.roomies.model.CircleManager.getUserCircleList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.roomies.LoginActivity;
import com.example.roomies.model.UserCircle;
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
                ((Activity)context).finish();
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

    // get user's points in circle
    public static int getPoints(ParseUser user){
        List<UserCircle> list = getUserCircleList();
        for(UserCircle uc : list){
            if(uc.getUser().getObjectId().equals(user.getObjectId())){
                return uc.getPoints();
            }
        }
        return 0;
    }

    // add to user's points in circle
    public static boolean addPoints(ParseUser user, int num){
        List<UserCircle> list = getUserCircleList();
        for(UserCircle uc : list){
            if(uc.getUser().getObjectId().equals(user.getObjectId())){
                uc.addPoints(num);
                uc.saveInBackground(e -> {
                    Log.i(TAG, "added points");
                });
                return true;
            }
        }
        return false;
    }

    /**
     * Reset password by sending email
     * @param context
     * @param email
     */
    public static void passwordReset(Context context, String email) {
        // An e-mail will be sent with further instructions
        ParseUser.requestPasswordResetInBackground(email, e -> {
            if (e == null) {
                // An email was successfully sent with reset instructions.
                Toast.makeText(context, "Email sent!", Toast.LENGTH_SHORT).show();
            } else {
                // Something went wrong. Look at the ParseException to see what's up.
            }
        });
    }
}

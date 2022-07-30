package com.example.roomies.utils;
import static com.example.roomies.model.CircleManager.getUserCircleList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.roomies.LoginActivity;
import com.example.roomies.MainActivity;
import com.example.roomies.model.UserCircle;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(context, "Incorrect login credentials", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<ParseUser> userList = new ArrayList<>();
                userList.add(user);
                ParseUser.pinAllInBackground(userList);

                Log.i(TAG, "login success " + ParseUser.getCurrentUser().getString("name"));
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


    public static void createAccount(Context context, String name, String email, String password, String password2){
        if(name.isEmpty()){
            Toast.makeText(context, "Name is a required field", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!isEmail(email)){
            Toast.makeText(context, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(password.length() < 8){
            Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!password.equals(password2)){
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(email);
        user.setPassword(password);
        user.setEmail(email);
        user.put("name", name);

        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    loginUser(context, email, password);
                    Intent i = new Intent(context, MainActivity.class);
                    context.startActivity(i);
                    ((Activity)context).finish();
                } else {
                    Log.e(TAG, "Sign up failed " + e);
                    Toast.makeText(context, "Sign up error: " + e, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private static boolean isEmail(String emailAddress) {
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }
}

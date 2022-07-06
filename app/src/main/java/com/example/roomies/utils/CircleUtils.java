package com.example.roomies.utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.roomies.AddCircleActivity;
import com.example.roomies.LoginActivity;
import com.example.roomies.model.Circle;
import com.example.roomies.model.UserCircle;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class CircleUtils {
    private static Circle currentCircle;
    private static boolean inCircle;
    private static List<UserCircle> userCircleList;
    private static UserCircle myUserCircle;

    public static final String TAG = "CircleUtils";

    /**
     * @return current Circle object
     */
    public static Circle getCurrentCircle(){
        return currentCircle;
    }

    /**
     * @return whether current user is in a circle
     */
    public static boolean getInCircle(){
        return inCircle;
    }

    /**
     * @return list of UserCircle objects related to current circle
     */
    public static List<UserCircle> getUserCircleList(){
        return userCircleList;
    }

    public static UserCircle getMyUserCircle() { return myUserCircle; }

    /**
     * clear locally stored circle information
     */
    public static void clearAll(){
        currentCircle = null;
        inCircle = false;
        userCircleList.clear();
    }

    /**
     * Query UserCircle objects that contain current user to get circles that user has joined
     * @param firstInit     true if also want to update chores and expenses
     */
    public static void initCircle(boolean firstInit){
        Log.i(TAG, "initCircle");

        // specify what type of data we want to query - UserCircle.class
        ParseQuery<UserCircle> query = ParseQuery.getQuery(UserCircle.class).whereEqualTo(UserCircle.KEY_USER, ParseUser.getCurrentUser());
        // include data referred by circle key
        query.include(UserCircle.KEY_CIRCLE);
        // start an asynchronous call for UserCircle objects that include current user

        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting userCircles", e);
                    inCircle = false;
                    return;
                }

                // user has not joined a circle
                if(userCircles.isEmpty()){
                    inCircle = false;
                    return;
                }

                // save received posts to list and notify adapter of new data
                currentCircle = userCircles.get(0).getCircle();
                inCircle = true;

                if(firstInit){
                    ChoreUtils.initChores();
                    ExpenseUtils.initExpenses();
                    CalendarDayUtils.initCalendar();
                }

                initUserCircleList();
            }
        });
    }

    /**
     * Query UserCircle objects related to current circle
     */
    private static void initUserCircleList(){
        if(userCircleList == null){
            userCircleList = new ArrayList<>();
        }

        // find profile of all users in current circle - query UserCircle.class
        ParseQuery<UserCircle> query = ParseQuery.getQuery(UserCircle.class).whereEqualTo(UserCircle.KEY_CIRCLE, currentCircle);
        // include data referred by user key
        query.include(UserCircle.KEY_USER);
        // start an asynchronous call for UserCircle objects that include current circle
        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting userCircles", e);
                    inCircle = false;
                    return;
                }

                // user has not joined a circle
                if(userCircles.isEmpty()){
                    inCircle = false;
                }

                // update userCircleList
                userCircleList.clear();
                userCircleList.addAll(userCircles);

                for(int i=0; i<userCircles.size(); i++){
                    String myObjectId = ParseUser.getCurrentUser().getObjectId();
                    if(userCircles.get(i).getUser().getObjectId().equals(myObjectId)){
                        myUserCircle = userCircles.get(i);
                        break;
                    }
                }
            }
        });
    }


    // delete userCircle object connection current user and their current circle
    public static void leaveCircle(Context context){
        // get userCircle object
        initCircle(false);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserCircle");
        UserCircle userCircle = getMyUserCircle();
        Circle circle = getCurrentCircle();

        // Retrieve the object by id
        query.getInBackground(userCircle.getObjectId(), (object, e) -> {
            if (e == null) {
                //Object was fetched
                //Deletes the fetched ParseObject from the database
                object.deleteInBackground(e2 -> {
                    if(e2==null){
                        Toast.makeText(context, "You have left circle " + circle.getName(), Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(context, AddCircleActivity.class);
                        context.startActivity(i);
                        ((Activity)context).finish();
                    }else{
                        //Something went wrong while deleting the Object
                        Toast.makeText(context, "Error leaving circle " + circle.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                //Something went wrong
                Toast.makeText(context, "Error retrieving circle, try again later", Toast.LENGTH_SHORT).show();
            }
        });
        clearAll();
    }

    public static void logout(Context context){
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
        SessionUtils.endSession();

        if(currentUser == null){
            Intent i = new Intent(context, LoginActivity.class);
            context.startActivity(i);
        }
    }
}

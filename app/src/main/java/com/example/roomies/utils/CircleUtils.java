package com.example.roomies.utils;
import android.util.Log;

import com.example.roomies.model.Circle;
import com.example.roomies.model.UserCircle;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class CircleUtils {
    private static Circle currentCircle;
    private static boolean inCircle;
    private static List<UserCircle> userCircleList;

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
            }
        });
    }
}

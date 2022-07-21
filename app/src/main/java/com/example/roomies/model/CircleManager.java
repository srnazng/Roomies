package com.example.roomies.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.roomies.AddCircleActivity;
import com.example.roomies.utils.CalendarDayUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import bolts.Task;

public class CircleManager {
    private static Circle currentCircle;
    private static List<UserCircle> userCircleList;
    private static UserCircle myUserCircle;

    private static ChoreCollection choreCollection;
    private static ExpenseCollection expenseCollection;

    public static final String TAG = "CircleManager";


    public static Circle getCurrentCircle(){
        return currentCircle;
    }

    public static List<UserCircle> getUserCircleList(){
        return userCircleList;
    }

    public static UserCircle getMyUserCircle() { return myUserCircle; }

    public static ChoreCollection getChoreCollection() { return choreCollection; }

    public static ExpenseCollection getExpenseCollection() { return expenseCollection; }

    /**
     * clear locally stored circle information
     */
    public static void clearAll(){
        currentCircle = null;
        userCircleList.clear();
    }

    /**
     * Query UserCircle objects that contain current user to get circles that user has joined
     * @param firstInit     true if also want to update chores and expenses
     */
    public static void initCircle(boolean firstInit, Context context){
        Log.i(TAG, "INIT CIRCLE");

        // specify what type of data we want to query - UserCircle.class
        ParseQuery<UserCircle> query = ParseQuery.getQuery(UserCircle.class).whereEqualTo(UserCircle.KEY_USER, ParseUser.getCurrentUser());
        // include data referred by circle key
        query.include(UserCircle.KEY_CIRCLE);
        // start an asynchronous call for UserCircle objects that include current user

        // query from local datastore then network
        query.fromLocalDatastore().findInBackground().continueWithTask((task) -> {
            // local results
            ParseException e = (ParseException) task.getError();
            if(e == null){
                Log.i(TAG, "circle from local storage");
                userCircleSetup(context, firstInit, task.getResult());
            }
            else{
                Log.e(TAG, "Issue with getting userCircles locally", e);
            }
            return query.fromNetwork().findInBackground();
        }, Task.UI_THREAD_EXECUTOR).continueWithTask((task) -> {
            // network results
            ParseException e = (ParseException) task.getError();
            if(e == null){
                Log.i(TAG, "circle from network");
                List<UserCircle> userCircles = task.getResult();
                userCircleSetup(context, firstInit, userCircles);
            }
            else{
                Log.e(TAG, "Issue with getting userCircles from network", e);
            }
            return task;
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * Called once UserCircles retrieved
     * @param firstInit
     * @param userCircles
     */
    public static void userCircleSetup(Context context, boolean firstInit, List<UserCircle> userCircles) throws ParseException {
        // user has not joined a circle
        if(!userCircles.isEmpty()){
            Log.i(TAG, "userCircleSetup " + userCircles.get(0).getCircle().getName());
            // save received posts to list and notify adapter of new data
            currentCircle = userCircles.get(0).getCircle();
            if(firstInit){
                choreCollection = new ChoreCollection(context);
                expenseCollection = new ExpenseCollection(context);
                CalendarDayUtils.initCalendar();
            }
            initUserCircleList(context);
        }
        else{
            Log.e(TAG, "No user circle");
        }
    }

    /**
     * Query UserCircle objects related to current circle
     */
    private static void initUserCircleList(Context context){
        if(userCircleList == null){
            userCircleList = new ArrayList<>();
        }

        // find profile of all users in current circle - query UserCircle.class
        ParseQuery<UserCircle> query = ParseQuery.getQuery(UserCircle.class).whereEqualTo(UserCircle.KEY_CIRCLE, currentCircle);
        // include data referred by user key
        query.include(UserCircle.KEY_USER);
        // start an asynchronous call for UserCircle objects that include current circle
        query.fromLocalDatastore().findInBackground().continueWithTask((task) -> {
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting userCircles locally", e);
            }
            else{
                userCircleListSetup(task.getResult());
            }
            return query.fromNetwork().findInBackground();
        }, Task.UI_THREAD_EXECUTOR).continueWithTask((task) -> {
            // Update UI with results from Network ...
            Log.i(TAG, "GET CIRCLE FROM NETWORK");
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting userCircles from network", e);
            }
            else{
                userCircleListSetup(task.getResult());
                UserCircle.pinAllInBackground(task.getResult());
            }
            return task;
        }, ContextCompat.getMainExecutor(context));
    }

    private static void userCircleListSetup(List<UserCircle> userCircles) throws ParseException {
        // user has not joined a circle
        if(!userCircles.isEmpty()){
            Log.i(TAG, "userCircleListSetup: " + userCircles.toString());

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
    }

    // delete userCircle object connection current user and their current circle
    public static void leaveCircle(Context context){
        // get userCircle object
        initCircle(false, context);

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

                        // user no longer registered to previous circle notifications
                        ParseUser.getCurrentUser().put("registerCircleNotifs", false);
                        ParseUser.getCurrentUser().saveInBackground();

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
}

package com.example.roomies.utils;

import android.util.Log;
import android.widget.Toast;

import com.example.roomies.model.Chore;
import com.example.roomies.model.ChoreAssignment;
import com.example.roomies.model.UserCircle;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ChoreUtils {
    private static List<Chore> myChores;
    private static List<Chore> circleChores;

    public static final String TAG = "ChoreUtils";

    /**
     * @return list of current user's chores (for current circle)
     */
    public static List<Chore> getMyChores(){
        return myChores;
    }

    /**
     * @return list of all chores of current circle
     */
    public static List<Chore> getCircleChores(){
        return circleChores;
    }

    /**
     * Add chore to circleChores list
     * @param c     Chore to add
     */
    public static void addCircleChore(Chore c){
        circleChores.add(c);
    }

    /**
     * Initialize all chore lists
     */
    public static void initChores(){
        initCircleChores();
        initMyChores();
    }

    /**
     * Clear all chore lists locally
     */
    public static void clearAll(){
        myChores.clear();
        circleChores.clear();
    }

    /**
     * Initialize current user's chores (for current circle)
     */
    public static void initMyChores(){
        if(myChores == null){
            myChores = new ArrayList<>();
        }

        // only get chore assignments of current user
        ParseQuery<ChoreAssignment> query = ParseQuery.getQuery(ChoreAssignment.class).whereEqualTo(ChoreAssignment.KEY_USER, ParseUser.getCurrentUser());
        // include chore and recurrence objects
        query.include(ChoreAssignment.KEY_CHORE);
        query.include(ChoreAssignment.KEY_CHORE + "." + Chore.KEY_RECURRENCE);
        // start an asynchronous call for ChoreAssignment objects
        query.findInBackground(new FindCallback<ChoreAssignment>() {
            @Override
            public void done(List<ChoreAssignment> choreAssignments, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting choreAssignments", e);
                    return;
                }

                // no chores
                if(choreAssignments == null || choreAssignments.isEmpty()){
                    Log.i(TAG, "No assigned chores");
                }
                else{
                    // save received chore assignments in this circle to list
                    myChores.clear();
                    for(int i=0; i<choreAssignments.size(); i++) {
                        ChoreAssignment c = choreAssignments.get(i);
                        if(c.getChore().getCircle().getObjectId().equals(CircleUtils.getCurrentCircle().getObjectId())){
                            myChores.add(c.getChore());
                        }
                    }
                }
            }
        });
    }

    /**
     * Initialize list of all chores for current circle
     */
    public static void initCircleChores(){
        if(circleChores == null){
            circleChores = new ArrayList<>();
        }

        // only get chores for user's current circle
        ParseQuery<Chore> query = ParseQuery.getQuery(Chore.class).whereEqualTo(Chore.KEY_CIRCLE, CircleUtils.getCurrentCircle());
        // include data referred by user key
        query.include(UserCircle.KEY_USER);
        // start an asynchronous call for Chore objects
        query.findInBackground(new FindCallback<Chore>() {
            @Override
            public void done(List<Chore> chores, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting userCircles", e);
                    return;
                }

                // no chores
                if(chores.isEmpty()){
                    Log.i(TAG, "No chores");
                }

                // save received chores to list and notify adapter of new data
                circleChores.clear();
                circleChores.addAll(chores);
            }
        });
    }
}

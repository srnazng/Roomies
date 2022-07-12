package com.example.roomies.utils;

import static com.example.roomies.utils.CircleUtils.getCurrentCircle;
import static com.example.roomies.utils.Utils.clearTime;
import static com.example.roomies.utils.Utils.compareDates;
import static com.example.roomies.utils.Utils.occursToday_dayFreq;
import static com.example.roomies.utils.Utils.occursToday_monthFreq;
import static com.example.roomies.utils.Utils.occursToday_weekFreq;

import android.util.Log;
import android.widget.Toast;

import com.example.roomies.model.Chore;
import com.example.roomies.model.ChoreAssignment;
import com.example.roomies.model.ChoreCompleted;
import com.example.roomies.model.Recurrence;
import com.example.roomies.model.UserCircle;
import com.google.android.material.card.MaterialCardView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChoreUtils {
    private static List<Chore> myChores;
    private static List<Chore> myChoresToday;
    private static List<Chore> myCompletedChoresToday;
    private static List<Chore> myPendingChoresToday;
    private static List<ChoreCompleted> completionsToday;
    private static List<ChoreAssignment> myChoreAssignments;
    private static List<Chore> circleChores;
    private static List<Chore> circleChoresToday;

    public static final String TAG = "ChoreUtils";

    public static List<Chore> getMyChores(){
        return myChores;
    }

    public static List<Chore> getMyChoresToday() { return myChoresToday; }

    public static List<Chore> getCircleChores(){
        return circleChores;
    }

    public static List<Chore> getMyPendingChoresToday() { return myPendingChoresToday; }

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
        myChoresToday.clear();
        myChoreAssignments.clear();
        circleChores.clear();
    }

    /**
     * Initialize current user's chores (for current circle)
     */
    public static void initMyChores(){
        if(myChores == null){
            myChores = new ArrayList<>();
        }
        if(myChoresToday == null){
            myChoresToday = new ArrayList<>();
        }
        if(myPendingChoresToday == null){
            myPendingChoresToday = new ArrayList<>();
        }
        if(myCompletedChoresToday == null){
            myCompletedChoresToday = new ArrayList<>();
        }
        if(myChoreAssignments == null){
            myChoreAssignments = new ArrayList<>();
        }
        if(completionsToday == null){
            completionsToday = new ArrayList<>();
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
                    updateCompletions(choreAssignments);
                }
            }
        });
    }

    /**
     * Mark cards as completed
     * @param choreAssignments
     */
    private static void updateCompletions(List<ChoreAssignment> choreAssignments){
        if(completionsToday == null){
            completionsToday = new ArrayList<>();
        }

        completionsToday.clear();

        // save received chore assignments in this circle to list
        Calendar today = Calendar.getInstance();
        clearTime(today);
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        clearTime(tomorrow);
        ParseQuery<ChoreCompleted> query = ParseQuery.getQuery(ChoreCompleted.class)
                .whereEqualTo(ChoreCompleted.KEY_CIRCLE, getCurrentCircle())
                .whereGreaterThanOrEqualTo("date", today.getTime())
                .whereLessThan("date", tomorrow.getTime());
        query.include(ChoreCompleted.KEY_CHORE_ASSIGNMENT);
        // start an asynchronous call for Chore objects
        query.findInBackground(new FindCallback<ChoreCompleted>() {
            @Override
            public void done(List<ChoreCompleted> choreCompletedList, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting choreCompleted", e);
                    return;
                }

                // no chores
                if(!choreCompletedList.isEmpty()){
                    completionsToday.addAll(choreCompletedList);
                }

                if(choreAssignments != null){
                    categorizeChores(choreAssignments);
                }
            }
        });

    }

    /**
     * filter chores into respective lists
     * @param choreAssignments
     */
    private static void categorizeChores(List<ChoreAssignment> choreAssignments){
        myChores.clear();
        myChoresToday.clear();
        myPendingChoresToday.clear();
        myCompletedChoresToday.clear();
        myChoreAssignments.clear();
        for(int i=0; i<choreAssignments.size(); i++) {
            ChoreAssignment c = choreAssignments.get(i);
            if(c.getChore().getCircle().getObjectId().equals(getCurrentCircle().getObjectId())){
                myChores.add(c.getChore());
                myChoreAssignments.add(c);
                if(occursOnDay(Calendar.getInstance(), c)){
                    myChoresToday.add(c.getChore());
                    if(isCompleted(c)){
                        myCompletedChoresToday.add(c.getChore());
                    }
                    else{
                        myPendingChoresToday.add(c.getChore());
                    }
                }
            }
        }
    }

    /**
     * return whether ChoreAssignment is completed
     * @param choreAssignment
     * @return
     */
    private static boolean isCompleted(ChoreAssignment choreAssignment){
        for(int i=0; i<completionsToday.size(); i++){
            if(completionsToday.get(i).getChoreAssignment().getObjectId()
                    .equals(choreAssignment.getObjectId())
                    && completionsToday.get(i).getCompleted()){
                return true;
            }
        }
        return false;
    }

    /**
     * Initialize list of all chores for current circle
     */
    public static void initCircleChores(){
        if(circleChores == null){
            circleChores = new ArrayList<>();
        }

        // only get chores for user's current circle
        ParseQuery<Chore> query = ParseQuery.getQuery(Chore.class).whereEqualTo(Chore.KEY_CIRCLE, getCurrentCircle());
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

    /**
     * Determine if assigned chore occurs on given day
     * Accounts for recurrence
     * @param day
     * @param choreAssignment
     * @return
     */
    public static boolean occursOnDay(Calendar day, ChoreAssignment choreAssignment){
        Chore chore = choreAssignment.getChore();

        // chore due date
        Calendar due = Calendar.getInstance();
        due.setTime(chore.getDue());
        clearTime(due);

        // today
        Calendar today = Calendar.getInstance();
        today.setTime(day.getTime());
        clearTime(today);

        // check if due today
        if(compareDates(today, due) == 0){
            return true;
        }

        // check if recurs today
        Recurrence recurrence = chore.getRecurrence();
        Calendar endRecurrenceDate = Calendar.getInstance();

        if(recurrence != null){
            endRecurrenceDate.setTime(recurrence.getEndDate());
            clearTime(endRecurrenceDate);
        }
        else{
            return false;
        }

        // check if recurrence already ended
        if(compareDates(today, endRecurrenceDate) > 0 || compareDates(today, due) < 0){
            return false;
        }

        // daily recurrence
        if(recurrence.getFrequencyType().equals(Recurrence.TYPE_DAY)
                && occursToday_dayFreq(due, recurrence.getFrequency(), today)){
            return true;
        }
        // weekly recurrence
        else if(recurrence.getFrequencyType().equals(Recurrence.TYPE_WEEK)
                && occursToday_weekFreq(due, recurrence.getDaysOfWeek(), recurrence.getFrequency(), today)){
            return true;
        }
        // recurrence with monthly frequency
        else if(recurrence.getFrequencyType().equals(Recurrence.TYPE_MONTH)){
            if(today.get(Calendar.DAY_OF_MONTH) == due.get(Calendar.DAY_OF_MONTH) &&
                    occursToday_monthFreq(due, recurrence.getFrequency(), today)){
                return true;
            }
        }
        // recurrence with yearly frequency
        else {
            if(today.get(Calendar.MONTH) == due.get(Calendar.MONTH)
                    && today.get(Calendar.DAY_OF_MONTH) == due.get(Calendar.DAY_OF_MONTH)
                    && (today.get(Calendar.YEAR) - due.get(Calendar.YEAR)) % recurrence.getFrequency() == 0){
                return true;
            }
        }

        return false;
    }

    /**
     * Update completed status of ChoreAssignment of the day
     * @param chore
     * @param completed
     */
    public static void markCompleted(Chore chore, boolean completed){
        ChoreAssignment choreAssignment = getChoreAssignment(chore);

        if(choreAssignment == null){
            return;
        }

        // only get ChoreCompleted objects pertaining to the ChoreAssignment of chore
        ParseQuery<ChoreCompleted> query = ParseQuery.getQuery(ChoreCompleted.class).whereEqualTo(ChoreCompleted.KEY_CHORE_ASSIGNMENT, choreAssignment);

        // start an asynchronous call for ChoreCompleted objects
        query.findInBackground(new FindCallback<ChoreCompleted>() {
            @Override
            public void done(List<ChoreCompleted> chores, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting ChoreCompleted objects", e);
                    return;
                }

                // no ChoreCompleted objects for ChoreAssignment
                if(chores.isEmpty()){
                    ChoreCompleted entity = new ChoreCompleted();

                    entity.put("date", Calendar.getInstance().getTime()); // date due
                    entity.put("choreAssignment", choreAssignment);
                    entity.put("completed", completed);
                    entity.put("circle", getCurrentCircle());

                    // Saves the new object.
                    // Notice that the SaveCallback is totally optional!
                    entity.saveInBackground(e2 -> {
                        if (e2==null){
                            //Save was done
                            updateCompletions(null);
                        }else{
                            //Something went wrong
                            Log.e(TAG, e2.getMessage());
                        }
                    });
                }
                else{
                    ChoreCompleted c = chores.get(0);
                    c.put("completed", completed);
                    c.saveInBackground();
                }
            }
        });

    }

    public static ChoreAssignment getChoreAssignment(Chore chore){
        for(int i=0; i<myChoreAssignments.size(); i++){
            if(myChoreAssignments.get(i).getChore().getObjectId().equals(chore.getObjectId())){
                return myChoreAssignments.get(i);
            }
        }
        return null;
    }

    public static void findChoreCompleted(Chore chore, MaterialCardView card){
        ChoreAssignment choreAssignment = getChoreAssignment(chore);
        // only get chores for user's current circle
        ParseQuery<ChoreCompleted> query = ParseQuery.getQuery(ChoreCompleted.class).whereEqualTo(ChoreCompleted.KEY_CHORE_ASSIGNMENT, choreAssignment);

        // start an asynchronous call for Chore objects
        query.findInBackground(new FindCallback<ChoreCompleted>() {
            @Override
            public void done(List<ChoreCompleted> chores, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting ChoreCompleted objects", e);
                    return;
                }

                // no chores
                if(chores.isEmpty()){
                    card.setChecked(false);
                }
                else{
                    ChoreCompleted c = chores.get(0);
                    if(c.getBoolean(ChoreCompleted.KEY_COMPLETED)){
                        card.setChecked(true);
                    }
                    else{
                        card.setChecked(false);
                    }
                }
            }
        });
    }
}

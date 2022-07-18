package com.example.roomies.utils;

import static com.example.roomies.ChoreFragment.updateChoreList;
import static com.example.roomies.utils.CircleUtils.getCurrentCircle;
import static com.example.roomies.utils.Utils.clearTime;
import static com.example.roomies.utils.Utils.compareDates;
import static com.example.roomies.utils.Utils.occursToday_dayFreq;
import static com.example.roomies.utils.Utils.occursToday_monthFreq;
import static com.example.roomies.utils.Utils.occursToday_weekFreq;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.example.roomies.ChoreDetailActivity;
import com.example.roomies.R;
import com.example.roomies.model.Chore;
import com.example.roomies.model.ChoreAssignment;
import com.example.roomies.model.ChoreCompleted;
import com.example.roomies.model.Recurrence;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
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
    private static List<ChoreAssignment> circleChoreAssignments;
    private static List<Chore> circleChores;
    private static List<Chore> circleChoresToday;

    public static final String TAG = "ChoreUtils";

    public static List<Chore> getMyChores(){
        return myChores;
    }

    public static List<Chore> getMyChoresToday() { return myChoresToday; }

    public static List<Chore> getCircleChores(){ return circleChores; }

    public static List<Chore> getMyPendingChoresToday() { return myPendingChoresToday; }

    public static List<Chore> getMyCompletedChoresToday() { return myCompletedChoresToday; }

    /**
     * Add chore to circleChores list
     * @param c     Chore to add
     */
    public static void addCircleChore(Chore c){
        circleChores.add(c);
    }

    /**
     * Add to chore assignment lists
     * @param c
     */
    public static void addChoreAssignment(ChoreAssignment c){
        if(c.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
            myChores.add(c.getChore());
            myChoreAssignments.add(c);
            if(occursOnDay(Calendar.getInstance(), c)){
                myChoresToday.add(c.getChore());
                myPendingChoresToday.add(c.getChore());
            }
        }
        circleChoreAssignments.add(c);
    }

    /**
     * Set colors of circle image indicating priority
     * @param context
     * @param ivCircle
     * @param chore
     */
    public static void setPriorityColors(Context context, ImageView ivCircle, Chore chore){
        if(chore.getPriority().equals(Chore.PRIORITY_HIGH)){
            ivCircle.setColorFilter(ContextCompat.getColor(context, R.color.orange), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else if(chore.getPriority().equals(Chore.PRIORITY_MED)){
            ivCircle.setColorFilter(ContextCompat.getColor(context, R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else{
            ivCircle.setColorFilter(ContextCompat.getColor(context, R.color.green), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    /**
     * Initialize all chore lists
     */
    public static void initChores(){
        Log.i(TAG, "INIT CHORES");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                initCircleChores();
            }
        });
        thread.start();
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
                else{
                    categorizeChores(myChoreAssignments);
                }
            }
        });
    }

    /**
     * filter chores into respective lists
     * @param choreAssignments
     */
    private static void categorizeChores(List<ChoreAssignment> choreAssignments){
        myChoresToday.clear();
        myPendingChoresToday.clear();
        myCompletedChoresToday.clear();

        if(getCurrentCircle() == null){
            return;
        }

        for(int i=0; i<choreAssignments.size(); i++) {
            ChoreAssignment c = choreAssignments.get(i);

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

        updateChoreList();
    }

    /**
     * return whether ChoreAssignment is completed
     * @param choreAssignment
     * @return
     */
    public static boolean isCompleted(ChoreAssignment choreAssignment){
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
     * Mark chip as completed
     * @param choreAssignment
     * @param chip
     * @param today
     */
    public static void chipCompleted(ChoreAssignment choreAssignment, Chip chip, Calendar today){
        // find time period of completion
        clearTime(today);
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(today.getTime());
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        clearTime(tomorrow);

        // query ChoreCompleted objects
        ParseQuery<ChoreCompleted> query = ParseQuery.getQuery(ChoreCompleted.class)
                .whereEqualTo(ChoreCompleted.KEY_CHORE_ASSIGNMENT, choreAssignment)
                .whereGreaterThanOrEqualTo("date", today.getTime())
                .whereLessThan("date", tomorrow.getTime());
        query.include(ChoreCompleted.KEY_CHORE_ASSIGNMENT);
        // start an asynchronous call for ChoreCompleted objects
        query.findInBackground(new FindCallback<ChoreCompleted>() {
            @Override
            public void done(List<ChoreCompleted> choreCompletedList, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting choreCompleted", e);
                    return;
                }

                // mark chip
                if(!choreCompletedList.isEmpty() && choreCompletedList.get(0).getCompleted()){
                    chip.setChecked(true);
                }
                else{
                    chip.setChecked(false);
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
        if(circleChoreAssignments == null){
            circleChoreAssignments = new ArrayList<>();
        }
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

        // only get chore assignments for user's current circle
        ParseQuery<ChoreAssignment> query = ParseQuery.getQuery(ChoreAssignment.class).whereEqualTo(ChoreAssignment.KEY_CIRCLE, getCurrentCircle());
        // include objects
        query.include(ChoreAssignment.KEY_USER);
        query.include(ChoreAssignment.KEY_CHORE);
        query.include(ChoreAssignment.KEY_CHORE + "." + Chore.KEY_RECURRENCE);

        // start an asynchronous call for ChoreAssignment objects
        query.findInBackground(new FindCallback<ChoreAssignment>() {
            @Override
            public void done(List<ChoreAssignment> chores, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting ChoreAssignments", e);
                    return;
                }

                // no chores
                if(chores.isEmpty()){
                    Log.i(TAG, "No chores");
                }

                circleChores.clear();
                circleChoreAssignments.clear();
                myChoreAssignments.clear();
                myChores.clear();

                // add to lists
                String myObjectId = ParseUser.getCurrentUser().getObjectId();
                for(int i=0; i<chores.size(); i++){
                    ChoreAssignment c = chores.get(i);
                    circleChoreAssignments.add(c);
                    if(!choreExists(c.getChore(), circleChores)){
                        circleChores.add(c.getChore());
                    }
                    if(c.getUser().getObjectId().equals(myObjectId)){
                        myChoreAssignments.add(c);
                        myChores.add(c.getChore());
                    }
                }
                updateCompletions(myChoreAssignments);
            }
        });
    }

    /**
     * Determine if a chore is already in a list
     * @param chore
     * @param list
     * @return
     */
    public static boolean choreExists(Chore chore, List<Chore> list){
        for(int i=0; i<list.size(); i++){
            if(chore.getObjectId().equals(list.get(i).getObjectId())){
                return true;
            }
        }
        return false;
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
    public static void markCompleted(Chore chore, boolean completed, Calendar day){
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

                    entity.put("date", day.getTime()); // date due
                    entity.put("choreAssignment", choreAssignment);
                    entity.put("completed", completed);
                    entity.put("circle", getCurrentCircle());

                    // Saves the new object.
                    // Notice that the SaveCallback is totally optional!
                    entity.saveInBackground(e2 -> {
                        if (e2==null){
                            //Save was done
                        }else{
                            //Something went wrong
                            Log.e(TAG, e2.getMessage());
                        }
                    });
                }
                else{
                    ChoreCompleted c = chores.get(0);
                    c.setCompleted(completed);
                    c.saveInBackground();

                    // mark ChoreCompleted as completed
                    for(int i=0; i<completionsToday.size(); i++){
                        if(completionsToday.get(i).getObjectId().equals(c.getObjectId())){
                            c.setCompleted(completed);
                            break;
                        }
                    }
                    updateCompletions(null);
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
        Log.i(TAG, "No chore assignment");
        return null;
    }

    /**
     * get list of ChoreAssignments correlated to chore
     * @param chore
     * @return
     */
    public static List<ChoreAssignment> getAllChoreAssignments(Chore chore){
        List<ChoreAssignment> list = new ArrayList<>();
        for(int i=0; i<circleChoreAssignments.size(); i++){
            if(circleChoreAssignments.get(i).getChore().getObjectId().equals(chore.getObjectId())){
                list.add(circleChoreAssignments.get(i));
            }
        }
        return list;
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

    /**
     * Go to detail page of chore
     * @param context
     * @param chore
     * @param day
     */
    public static void toDetail(Context context, Chore chore, Calendar day){
        Intent i = new Intent(context, ChoreDetailActivity.class);
        i.putExtra("chore", chore);
        i.putExtra("day", day);
        context.startActivity(i);
    }
}
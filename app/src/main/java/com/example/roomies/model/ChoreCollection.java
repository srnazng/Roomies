package com.example.roomies.model;

import static com.example.roomies.ChoreFragment.updateChoreList;
import static com.example.roomies.model.CircleManager.getChoreCollection;
import static com.example.roomies.model.CircleManager.getCurrentCircle;
import static com.example.roomies.utils.CalendarDayUtils.occursToday_dayFreq;
import static com.example.roomies.utils.CalendarDayUtils.occursToday_monthFreq;
import static com.example.roomies.utils.CalendarDayUtils.occursToday_weekFreq;
import static com.example.roomies.utils.Utils.clearTime;
import static com.example.roomies.utils.Utils.compareDates;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.roomies.GoogleSignInActivity;
import com.example.roomies.utils.UserUtils;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChoreCollection {
    private List<Chore> myChores;
    private List<Chore> myChoresToday;
    private List<Chore> myCompletedChoresToday;
    private List<Chore> myPendingChoresToday;

    private List<ChoreCompleted> completionsToday;

    private List<ChoreAssignment> myChoreAssignments;
    private List<ChoreAssignment> circleChoreAssignments;
    private List<Chore> circleChores;

    public static final String TAG = "ChoreCollection";

    public ChoreCollection(){}

    public ChoreCollection(Context context){
        circleChores = new ArrayList<>();
        circleChoreAssignments = new ArrayList<>();
        myChores = new ArrayList<>();
        myChoresToday = new ArrayList<>();
        myPendingChoresToday = new ArrayList<>();
        myCompletedChoresToday = new ArrayList<>();
        myChoreAssignments = new ArrayList<>();
        completionsToday = new ArrayList<>();
        initChores(context);
    }

    public List<Chore> getMyChores(){ return myChores;}

    public List<Chore> getMyChoresToday() { return myChoresToday; }

    public List<Chore> getCircleChores(){ return circleChores; }

    public List<Chore> getMyPendingChoresToday() { return myPendingChoresToday; }

    public List<Chore> getMyCompletedChoresToday() { return myCompletedChoresToday; }

    /**
     * Add chore to circleChores list
     * @param c     Chore to add
     */
    public void addCircleChore(Chore c){
        circleChores.add(c);
    }

    /**
     * Add to chore assignment lists
     * @param c
     */
    public void addChoreAssignment(ChoreAssignment c){
        if(c.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
            myChores.add(c.getChore());
            myChoreAssignments.add(c);
            if(occursOnDay(Calendar.getInstance(), c)){
                myChoresToday.add(c.getChore());
                myPendingChoresToday.add(c.getChore());
            }
        }
        circleChoreAssignments.add(c);

        List<ChoreAssignment> assignments = new ArrayList<>();
        assignments.add(c);
        ChoreAssignment.pinAllInBackground(assignments);
    }

    /**
     * Initialize all chore lists
     */
    public void initChores(Context context){
        Log.i(TAG, "INIT CHORES");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                initCircleChores(context);
            }
        });
        thread.start();
    }

    /**
     * Clear all chore lists locally
     */
    public void clearAll(){
        myChores.clear();
        myChoresToday.clear();
        myChoreAssignments.clear();
        circleChores.clear();
    }

    public void submitChore(Chore entity,
                            boolean sendInvites,
                            List<ParseUser> assignedUsers,
                            ArrayList<String> assignedEmails,
                            Context context){
        // Saves the new object.
        entity.saveInBackground(e -> {
            if (e==null){
                //Save was done
                List<Chore> list = new ArrayList<>();
                list.add(entity);
                Chore.pinAllInBackground(list);
                assignChores(entity, sendInvites, assignedUsers, assignedEmails, context);
            }else{
                //Something went wrong
                Toast.makeText(context, "Could not add chore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateChore(Chore entity,
                            boolean sendInvites,
                            List<ChoreAssignment> originalChoreAssignments,
                            List<ParseUser> assignedUsers,
                            ArrayList<String> assignedEmails,
                            Context context){

        entity.setLastEditedBy(ParseUser.getCurrentUser());

        // Saves the new object.
        entity.saveInBackground(e -> {
            if (e==null){
                //Save was done
                List<Chore> list = new ArrayList<>();
                list.add(entity);
                Chore.pinAllInBackground(list);
                reassignChores(entity, sendInvites, originalChoreAssignments,
                        assignedUsers, assignedEmails, context);
            }else{
                //Something went wrong
                Toast.makeText(context, "Could not update chore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int listContainsUser(List<ChoreAssignment> list, ParseUser user){
        for(int i=0; i<list.size(); i++){
            if(list.get(i).getUser().getObjectId().equals(user.getObjectId())){
                return i;
            }
        }
        return -1;
    }

    public void reassignChores(Chore chore,
                               boolean sendInvites,
                               List<ChoreAssignment> originalChoreAssignments,
                               List<ParseUser> assignedUsers,
                               ArrayList<String> assignedEmails,
                               Context context){
        // loop through all assigned users
        for(int i=0; i<assignedUsers.size(); i++){
            int index = listContainsUser(originalChoreAssignments, assignedUsers.get(i));
            if(index >= 0){
                originalChoreAssignments.remove(index);
                continue;
            }
            ChoreAssignment entity = new ChoreAssignment();

            entity.put("user", assignedUsers.get(i));
            entity.put("chore", chore);
            entity.put("circle", getCurrentCircle());

            int finalI = i;
            entity.saveInBackground(e -> {
                if (e==null){
                    // Saves the new object.
                    getChoreCollection().addChoreAssignment(entity);

                    if(finalI == assignedUsers.size() - 1){
                        updateChoreList();
                    }
                }else{
                    //Something went wrong
                    Toast.makeText(context, "Error assigning chore", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.getMessage());
                    return;
                }
            });
        }

        for(ChoreAssignment c : originalChoreAssignments){
            c.deleteInBackground();
        }

        //done
        Toast.makeText(context, "Chore updated success", Toast.LENGTH_SHORT).show();
        getChoreCollection().addCircleChore(chore);

        // create Google Calendar event
        // send Google Calendar invites if needed
        if(sendInvites){
            Intent i = new Intent(context, GoogleSignInActivity.class);
            i.putExtra("chore", chore);
            Log.i(TAG, "final emails: " + assignedEmails.toString());
            i.putStringArrayListExtra("emails", assignedEmails);
            context.startActivity(i);
        }

        ((Activity)context).finish();
    }

    // create ChoreAssignment object for each user assigned chore and add to database
    public void assignChores(Chore chore,
                             boolean sendInvites,
                             List<ParseUser> assignedUsers,
                             ArrayList<String> assignedEmails,
                             Context context){

        // loop through all assigned users
        for(int i=0; i<assignedUsers.size(); i++){
            ChoreAssignment entity = new ChoreAssignment();

            entity.put("user", assignedUsers.get(i));
            entity.put("chore", chore);
            entity.put("circle", getCurrentCircle());

            int finalI = i;
            entity.saveInBackground(e -> {
                if (e==null){
                    // Saves the new object.
                    getChoreCollection().addChoreAssignment(entity);

                    if(finalI == assignedUsers.size() - 1){
                        updateChoreList();
                    }
                }else{
                    //Something went wrong
                    Toast.makeText(context, "Error assigning chore", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.getMessage());
                    return;
                }
            });
        }
        //done
        Toast.makeText(context, "Chore added success", Toast.LENGTH_SHORT).show();
        getChoreCollection().addCircleChore(chore);

        // create Google Calendar event
        // send Google Calendar invites if needed
        if(sendInvites){
            Intent i = new Intent(context, GoogleSignInActivity.class);
            i.putExtra("chore", chore);
            Log.i(TAG, "final emails: " + assignedEmails.toString());
            i.putStringArrayListExtra("emails", assignedEmails);
            context.startActivity(i);
        }

        ((Activity)context).finish();
    }

    /**
     * Mark cards as completed
     * @param choreAssignments
     */
    private void updateCompletions(Context context, List<ChoreAssignment> choreAssignments){
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
        // retrieve from local datastore and then from network
        query.fromLocalDatastore().findInBackground().continueWithTask((task) -> {
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting choreCompleted locally", e);
            }
            else{
                Log.i(TAG, "ChoreCompleted retrieved locally" + task.getResult().toString());
                setCompletions(task.getResult(), choreAssignments);
            }
            return query.fromNetwork().findInBackground();
        }, ContextCompat.getMainExecutor(context)).continueWithTask((task) -> {
            // Update UI with results from Network ...
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting choreCompleted from network", e);
            }
            else{
                Log.i(TAG, "ChoreCompleted retrieved from network" + task.getResult().toString());
                setCompletions(task.getResult(), choreAssignments);
                ChoreCompleted.pinAllInBackground(task.getResult());
                ChoreCompleted.pinAllInBackground(choreAssignments);
            }
            return task;
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * Update lists based on chore completion
     * @param choreCompletedList
     * @param choreAssignments
     */
    private void setCompletions(List<ChoreCompleted> choreCompletedList,
                                      List<ChoreAssignment> choreAssignments){
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

    /**
     * filter chores into respective lists
     * @param choreAssignments
     */
    private void categorizeChores(List<ChoreAssignment> choreAssignments){
        Log.i(TAG, "CATEGORIZE CHORES");

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
    public boolean isCompleted(ChoreAssignment choreAssignment){
        if(choreAssignment == null){
            return false;
        }
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
    public void initCircleChores(Context context){
        // only get chore assignments for user's current circle
        ParseQuery<ChoreAssignment> query = ParseQuery.getQuery(ChoreAssignment.class).whereEqualTo(ChoreAssignment.KEY_CIRCLE, getCurrentCircle());

        // include objects
        query.include(ChoreAssignment.KEY_USER);
        query.include(ChoreAssignment.KEY_CHORE);
        query.include(ChoreAssignment.KEY_CHORE + "." + Chore.KEY_RECURRENCE);
        query.orderByDescending(ChoreAssignment.KEY_UPDATED_AT);

        // start an asynchronous call for ChoreAssignment objects
        query.fromLocalDatastore().findInBackground().continueWithTask((task) -> {
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting ChoreAssignments locally", e);
            }
            else{
                Log.i(TAG, "ChoreAssignments retrieved locally");
                setChoreLists(context, task.getResult(), false);
            }
            return query.fromNetwork().findInBackground();
        }, ContextCompat.getMainExecutor(context)).continueWithTask((task) -> {
            // Update UI with results from Network ...
            Log.i(TAG, "FROM NETWORK");
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting ChoreAssignments from network", e);
            }
            else{
                Log.i(TAG, "ChoreAssignments retrieved from network");
                setChoreLists(context, task.getResult(), true);
            }
            return task;
        }, ContextCompat.getMainExecutor(context));
    }

    private void setChoreLists(Context context,
                                     List<ChoreAssignment> chores,
                                     boolean fromNetwork){
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
        updateCompletions(context, myChoreAssignments);

        if(fromNetwork){
            ChoreAssignment.pinAllInBackground(circleChoreAssignments);
            Chore.pinAllInBackground(circleChores);
        }
    }

    /**
     * Determine if a chore is already in a list
     * @param chore
     * @param list
     * @return
     */
    private boolean choreExists(Chore chore, List<Chore> list){
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
    private boolean occursOnDay(Calendar day, ChoreAssignment choreAssignment){
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
        else if(recurrence.getFrequencyType().equals(Recurrence.TYPE_MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == due.get(Calendar.DAY_OF_MONTH) &&
                occursToday_monthFreq(due, recurrence.getFrequency(), today)){
            return true;
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
    public void markCompleted(Context context, Chore chore, boolean completed, Calendar day){
        ChoreAssignment choreAssignment = getChoreAssignment(chore);
        
        addPoints(ParseUser.getCurrentUser(), chore, completed);

        if(choreAssignment == null){
            return;
        }

        // only get ChoreCompleted objects pertaining to the ChoreAssignment of chore
        ParseQuery<ChoreCompleted> query = ParseQuery.getQuery(ChoreCompleted.class).whereEqualTo(ChoreCompleted.KEY_CHORE_ASSIGNMENT, choreAssignment);

        // start an asynchronous call for ChoreCompleted objects
        query.fromLocalDatastore().findInBackground().continueWithTask((task) -> {
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting ChoreCompleted objects", e);
            }
            else{
                setNewCompletion(task.getResult(), choreAssignment, day, completed);
            }
            return query.fromNetwork().findInBackground();
        }, ContextCompat.getMainExecutor(context)).continueWithTask((task) -> {
            // Update UI with results from Network ...
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting ChoreCompleted objects", e);
            }
            else{
                setNewCompletion(task.getResult(), choreAssignment, day, completed);
            }
            return task;
        }, ContextCompat.getMainExecutor(context));
    }

    private boolean addPoints(ParseUser user, Chore chore, boolean completed){
        if(completed){
            return UserUtils.addPoints(user, chore.getPoints());
        }
        else{
            return UserUtils.addPoints(user, -1 * chore.getPoints());
        }
    }

    private void setNewCompletion(List<ChoreCompleted> chores,
                                        ChoreAssignment choreAssignment,
                                        Calendar day,
                                        boolean completed){
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

            List<ChoreCompleted> list = new ArrayList<>();
            list.add(entity);

            ChoreCompleted.pinAllInBackground(list);
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
            ChoreCompleted.pinAllInBackground(completionsToday);
        }
        categorizeChores(myChoreAssignments);
    }

    public ChoreAssignment getChoreAssignment(Chore chore){
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
    public List<ChoreAssignment> getAllChoreAssignments(Chore chore){
        List<ChoreAssignment> list = new ArrayList<>();
        for(int i=0; i<circleChoreAssignments.size(); i++){
            if(circleChoreAssignments.get(i).getChore().getObjectId().equals(chore.getObjectId())){
                list.add(circleChoreAssignments.get(i));
            }
        }
        return list;
    }
}
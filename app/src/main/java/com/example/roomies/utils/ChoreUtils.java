package com.example.roomies.utils;

import static com.example.roomies.utils.Utils.clearTime;
import static com.example.roomies.utils.Utils.getMonthForInt;

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
import com.google.android.material.chip.Chip;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.List;

public class ChoreUtils {
    public static final String TAG = "ChoreUtils";

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


    // set text of repeat button
    public static String getRepeatMessage(Recurrence recurrence,
                                          Calendar endDate,
                                          Integer numOccurrences){
        if(recurrence == null){
            return "Does not repeat";
        }

        String message = "Repeats every ";
        if(recurrence.getFrequency() > 1){
            message = message + recurrence.getFrequency() + " ";
        }

        message = message + recurrence.getFrequencyType();

        if(recurrence.getFrequency() > 1){
            message = message + "s";
        }

        if(endDate != null){
            int month = endDate.get(Calendar.MONTH);
            int day = endDate.get(Calendar.DAY_OF_MONTH);
            int year = endDate.get(Calendar.YEAR);
            message = message + " until " + getMonthForInt(month) + " " + day + ", " + year;
        }
        else if(numOccurrences != null){
            message = message + " until " + numOccurrences + " occurrences";
        }

        return message;
    }
}

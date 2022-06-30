package com.example.roomies.utils;

import android.util.Log;

import com.example.roomies.model.Chore;
import com.example.roomies.model.Expense;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ExpenseUtils {
    private static List<Expense> circleExpenses;

    public static final String TAG = "ExpenseUtils";

    /**
     * @return list of all expenses for current circle
     */
    public static List<Expense> getCircleExpenses(){
        return circleExpenses;
    }

    /**
     * Initialize all lists of expenses
     */
    public static void initExpenses(){
        initCircleExpenses();
        // TODO: get expenses only related to user
    }

    /**
     * Clear all locally stored lists of expenses
     */
    public static void clearAll(){
        circleExpenses.clear();
    }

    /**
     * Add expense to circleExpenses
     * @param e   Expense object to add
     */
    public static void addCircleExpense(Expense e){
        circleExpenses.add(e);
    }

    /**
     * Query expenses belonging to current circle
     */
    public static void initCircleExpenses(){
        if(circleExpenses == null){
            circleExpenses = new ArrayList<>();
        }

        // only get expenses from user's current circle
        ParseQuery<Expense> query = ParseQuery.getQuery(Expense.class).whereEqualTo(Chore.KEY_CIRCLE, CircleUtils.getCurrentCircle());
        // include receiver object
        query.include(Expense.KEY_CREATOR);
        // start an asynchronous call for Expense objects
        query.findInBackground(new FindCallback<Expense>() {
            @Override
            public void done(List<Expense> expenses, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting expenses", e);
                    return;
                }

                // no expenses
                if(expenses.isEmpty()){
                    Log.i(TAG, "No expenses");
                }

                // save received expenses to list and notify adapter of new data
                circleExpenses.clear();
                circleExpenses.addAll(expenses);
            }
        });
    }
}

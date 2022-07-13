package com.example.roomies.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.roomies.R;
import com.example.roomies.model.Chore;
import com.example.roomies.model.Expense;
import com.example.roomies.model.Transaction;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ExpenseUtils {
    private static List<Expense> circleExpenses;
    private static List<Expense> myExpenses;

    public static final String sumMessage = "\nTotal amount assigned: $";

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
        // initialize circleExpenses
        if(circleExpenses == null){
            circleExpenses = new ArrayList<>();
        }

        // no current circle
        if(CircleUtils.getCurrentCircle() == null){
            return;
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

    // remove dollar sign and commas from price string
    public static String removeDollar(String price){
        // remove dollar sign
        if(!price.isEmpty() && price.charAt(0) == '$'){
            price = price.substring(1);
        }

        // no price entered
        if(price.isEmpty()){
            return "0.00";
        }

        // remove commas
        return price.replaceAll(",", "");
    }

    // divide total cost evenly among everyone selected to pay expense
    public static boolean splitCost(Context context, String expenseTotal, List<View> transactionViews, TextView tvSplitSum, int numAssigned){
        // cannot split cost if total not entered
        if(expenseTotal.equals("$")){
            if(context != null){
                Toast.makeText(context, "Enter total price before dividing cost", Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        // no one assigned to pay expense
        if(numAssigned <= 0){
            if(context != null){
                Toast.makeText(context, "Assign expense before dividing cost", Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        // get total cost
        BigDecimal total = new BigDecimal(removeDollar(expenseTotal));
        if(total.compareTo(BigDecimal.ZERO) == 0){
            return false;
        }
        BigDecimal sum = new BigDecimal(0);

        // evenly assign amounts paid
        for(int i=0; i<transactionViews.size(); i++){
            View view = transactionViews.get(i);
            if(view != null){
                CheckBox checkName = view.findViewById(R.id.checkName);

                // user is assigned to help pay for this expense
                if(checkName.isChecked() ){
                    EditText etAmount = view.findViewById(R.id.etAmount);
                    String stringAmount = String.format("%.2f", total.divide(BigDecimal.valueOf(numAssigned), 2, RoundingMode.FLOOR));
                    etAmount.setText(stringAmount);
                    sum = sum.add(new BigDecimal(stringAmount));
                }
            }
        }

        // if not even split
        while(numAssigned > 0
                && sum.compareTo(total) < 0
                && transactionViews != null
                && transactionViews.get(0) != null){
            for(int i=0; i<transactionViews.size(); i++){
                View view = transactionViews.get(i);
                if(view != null){
                    CheckBox checkName = view.findViewById(R.id.checkName);
                    // user is assigned to help pay for this expense and is not current user
                    if(checkName.isChecked() ){
                        EditText etAmount = view.findViewById(R.id.etAmount);
                        BigDecimal prevAmount = new BigDecimal(removeDollar(etAmount.getText().toString()));
                        String stringAmount = String.format("%.2f",  prevAmount.add(BigDecimal.valueOf(0.01)));
                        etAmount.setText(stringAmount);
                        sum = sum.add(BigDecimal.valueOf(0.01));

                        // enough is assigned
                        if(sum.compareTo(total) >= 0){
                            // calculate total assigned
                            findAssignedSum(tvSplitSum, transactionViews);
                            return true;
                        }
                    }
                }
            }
        }

        // calculate total assigned
        findAssignedSum(tvSplitSum, transactionViews);
        return true;
    }

    // get the number of users in circle assigned ot pay expense
    public static int getNumAssigned(List<View> transactionViews){
        if(transactionViews == null || transactionViews.isEmpty()){
            return 0;
        }

        int numAssigned = 0;
        for(int i=0; i<transactionViews.size(); i++){
            View view = transactionViews.get(i);
            if(view != null){
                CheckBox checkName = view.findViewById(R.id.checkName);
                // user is assigned to help pay for this expense
                if(checkName.isChecked() ){
                    numAssigned++;
                }
            }
        }
        return numAssigned;
    }

    // show the total amount users are assigned to pay
    public static String findAssignedSum(TextView tvSplitSum, List<View> transactionViews){
        BigDecimal sum = new BigDecimal(0);
        if(transactionViews == null){
            return "$0.00";
        }
        for(int i=0; i<transactionViews.size(); i++){
            View view = transactionViews.get(i);
            if(view != null){
                CheckBox checkName = view.findViewById(R.id.checkName);

                // user is assigned to help pay for this expense and is not current user
                if(checkName.isChecked() ){
                    EditText etAmount = view.findViewById(R.id.etAmount);
                    sum = sum.add(new BigDecimal(removeDollar(etAmount.getText().toString())));
                }
            }
        }

        // update sum text
        if(tvSplitSum != null){
            tvSplitSum.setText(sumMessage + String.format("%.2f", sum));
        }

        return String.format("%.2f", sum);
    }


    // add Expense object on database
    public static void submitExpense(Context context,
                                     @NonNull EditText etExpenseName,
                                     EditText etTotal,
                                     boolean split,
                                     List<View> transactionViews,
                                     List<ParseUser> transactionUsers){
        // submit expense without expense name
        if(etExpenseName.getText().toString().isEmpty()){
            Toast.makeText(context, "Expense reason must not be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // submit expense without specifying total amount
        if(etTotal.getText().toString().equals("$")){
            Toast.makeText(context, "Expense total must not be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Expense entity = new Expense();
        entity.put("name", etExpenseName.getText().toString());
        entity.put("total", Float.parseFloat(removeDollar(etTotal.getText().toString())));
        entity.put("creator", ParseUser.getCurrentUser());
        entity.put("circle", CircleUtils.getCurrentCircle());

        // TODO: upload photo proof
        // entity.put("proof", new ParseFile("resume.txt", "My string content".getBytes()));

        Expense expense = entity;

        // Saves the new object.
        entity.saveInBackground(e -> {
            if (e==null){
                //Save was done
                if(split){
                    // assign expense to specific users
                    submitTransactions(context, transactionViews, transactionUsers, expense);
                }
                else{
                    // done
                    Toast.makeText(context, "Added expense", Toast.LENGTH_SHORT).show();
                    ExpenseUtils.addCircleExpense(expense);
                    ExpenseUtils.initExpenses();
                    ((Activity) context).finish();
                }
            }else{
                //Something went wrong
                Toast.makeText(context, "Error adding expense", Toast.LENGTH_LONG).show();
                Log.e(TAG, e.getMessage());
            }
        });
    }

    // add Transaction objects for each user assigned to pay expense
    public static void submitTransactions(Context context, List<View> transactionViews, List<ParseUser> transactionUsers, Expense expense){
        String currentUser = ParseUser.getCurrentUser().getObjectId();

        // loop through all users in circle
        for(int i=0; i<transactionViews.size(); i++){
            View view = transactionViews.get(i);
            CheckBox checkName = view.findViewById(R.id.checkName);

            // user is assigned to help pay for this expense and is not current user
            if(checkName.isChecked() && !transactionUsers.get(i).getObjectId().equals(currentUser)){
                EditText etAmount = view.findViewById(R.id.etAmount);
                Transaction entity = new Transaction();

                // current user
                entity.put("receiver", ParseUser.getCurrentUser());
                // assigned user
                entity.put("payer", transactionUsers.get(i));
                // amount specified
                if(!etAmount.getText().toString().equals("$")){
                    entity.put("amount", Float.parseFloat(removeDollar(etAmount.getText().toString())));
                }
                // related Expense object
                entity.put("expense", expense);
                // transaction status
                entity.put("completed", false);

                // Saves the new object.
                // Notice that the SaveCallback is totally optional!
                entity.saveInBackground(e -> {
                    if (e==null){
                        //Save was done
                    }else{
                        //Something went wrong
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(context, "Error making transaction", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        }

        // all transactions successfully saved
        Toast.makeText(context, "Added expense", Toast.LENGTH_SHORT).show();
        ExpenseUtils.addCircleExpense(expense);
        ExpenseUtils.initExpenses();
        ((Activity) context).finish();
    }

}

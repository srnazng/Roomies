package com.example.roomies.utils;

import static com.example.roomies.model.CircleManager.getExpenseCollection;
import static com.example.roomies.model.ExpenseCollection.getAllExpenseTransactions;
import static com.example.roomies.utils.Messaging.sendToDeviceGroup;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomies.R;
import com.example.roomies.adapter.ExpenseCommentsAdapter;
import com.example.roomies.model.Expense;
import com.example.roomies.model.ExpenseComment;
import com.example.roomies.model.Transaction;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ExpenseUtils {
    public static final String sumMessage = "\nTotal amount assigned: $";

    public static final String TAG = "ExpenseUtils";

    /**
     * @param transactionViews
     * @return number of users assigned to pay expense
     */
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

    /**
     * Create ExpenseComment object and save
     * @param expense
     * @param comment
     * @param expenseCommentList
     * @param adapter
     */
    public static void sendComment(Expense expense,
                            String comment,
                            List<ExpenseComment> expenseCommentList,
                            ExpenseCommentsAdapter adapter){
        ExpenseComment entity = new ExpenseComment();

        entity.put("expense", expense);
        entity.put("user", ParseUser.getCurrentUser());
        entity.put("comment", comment);

        // Saves ExpenseComment object
        entity.saveInBackground(e -> {
            if (e==null){
                //Save was done
                expenseCommentList.add(entity);
                adapter.notifyDataSetChanged();
                initComments(expense, expenseCommentList, adapter);
            }else{
                //Something went wrong
                Log.e(TAG, "Error sending comment");
            }
        });
    }

    /**
     * Initialize expenseCommentList with expense's ExpenseComments
     * @param expense
     * @param expenseCommentList
     * @param adapter
     */
    public static void initComments(Expense expense, List<ExpenseComment> expenseCommentList, ExpenseCommentsAdapter adapter){
        // only get ExpenseComments related to expense
        ParseQuery<ExpenseComment> query = ParseQuery.getQuery(ExpenseComment.class).whereEqualTo(ExpenseComment.KEY_EXPENSE, expense);
        // include user object
        query.include(ExpenseComment.KEY_USER);
        // start an asynchronous call for ExpenseComment objects
        query.findInBackground(new FindCallback<ExpenseComment>() {
            @Override
            public void done(List<ExpenseComment> comments, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting expenses", e);
                    return;
                }
                expenseCommentList.clear();
                expenseCommentList.addAll(comments);
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * remove dollar sign and commas from price string
     * @param price
     * @return reformatted price
     */
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

    /**
     * divide total cost evenly among everyone selected to pay expense
     * @param context
     * @param expenseTotal
     * @param transactionViews
     * @param tvSplitSum
     * @param numAssigned
     * @return whether expense was split successfully
     */
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


    /**
     * show the total amount users are assigned to pay
     * @param tvSplitSum
     * @param transactionViews
     * @return sum of amounts assigned as a String
     */
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


    /**
     * send reminder to all users assigned to pay expense but have not
     * @param context
     * @param expense
     */
    public static void sendReminder(Context context, Expense expense){
        List<Transaction> transactions = getAllExpenseTransactions(expense, getExpenseCollection().getCircleTransactions());
        for(int i=0; i<transactions.size(); i++){
            Transaction t = transactions.get(i);
            if(!t.getCompleted()){
                sendToDeviceGroup(t.getPayer().getString("notificationKey"), "House expense reminder!", "Pending request for " + expense.getName());
            }
        }
        Toast.makeText(context, "Reminders sent!", Toast.LENGTH_SHORT).show();
    }


}

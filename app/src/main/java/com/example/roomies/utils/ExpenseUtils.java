package com.example.roomies.utils;

import static com.example.roomies.ExpenseFragment.getFilterInt;
import static com.example.roomies.ExpenseFragment.updateExpenseList;
import static com.example.roomies.utils.CircleUtils.getCurrentCircle;
import static com.example.roomies.utils.Messaging.sendToDeviceGroup;
import static com.example.roomies.utils.Utils.conversionBitmapParseFile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.roomies.R;
import com.example.roomies.adapter.ExpenseCommentsAdapter;
import com.example.roomies.model.Chore;
import com.example.roomies.model.Expense;
import com.example.roomies.model.ExpenseComment;
import com.example.roomies.model.Transaction;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ExpenseUtils {
    private static List<Expense> circleExpenses;
    private static List<Transaction> circleTransactions;

    // paid by me
    private static List<Expense> myPendingPayments;
    private static List<Expense> myCompletedPayments;

    // paid to me
    private static List<Expense> myPendingRequests;
    private static List<Expense> myCompletedRequests;

    public static final String sumMessage = "\nTotal amount assigned: $";

    public static final String TAG = "ExpenseUtils";

    public static List<Expense> getCircleExpenses(){
        return circleExpenses;
    }

    public static List<Transaction> getCircleTransactions(){
        return circleTransactions;
    }

    public static List<Expense> getMyCompletedPayments(){
        return myCompletedPayments;
    }

    public static List<Expense> getMyPendingPayments(){
        return myPendingPayments;
    }

    public static List<Expense> getMyCompletedRequests(){
        return myCompletedRequests;
    }

    public static List<Expense> getMyPendingRequests(){
        return myPendingRequests;
    }

    /**
     * Initialize all lists of expenses
     */
    public static void initExpenses(){
        Log.i(TAG, "INIT EXPENSES");
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                initCircleExpenses();
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                initCircleTransactions();
            }
        });

        thread1.start();
        thread2.start();
    }

    /**
     * Clear all locally stored lists of expenses
     */
    public static void clearAll(){
        circleExpenses.clear();
        circleTransactions.clear();
        myCompletedPayments.clear();
        myPendingPayments.clear();
        myCompletedRequests.clear();
        myPendingRequests.clear();
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
                    Log.i(TAG, "No circle expenses");
                }

                // save received expenses to list and notify adapter of new data
                circleExpenses.clear();
                circleExpenses.addAll(expenses);
                updateExpenseList(getFilterInt());
            }
        });
    }

    /**
     * Query transactions belonging to current circle
     */
    public static void initCircleTransactions(){
        if(circleTransactions == null){
            circleTransactions = new ArrayList<>();
        }
        if(myCompletedPayments == null){
            myCompletedPayments = new ArrayList<>();
        }
        if(myPendingPayments == null){
            myPendingPayments = new ArrayList<>();
        }
        if(myPendingRequests == null){
            myPendingRequests = new ArrayList<>();
        }
        if(myCompletedRequests == null){
            myCompletedRequests = new ArrayList<>();
        }

        // no current circle
        if(CircleUtils.getCurrentCircle() == null){
            return;
        }

        // only get expenses from user's current circle
        ParseQuery<Transaction> query = ParseQuery.getQuery(Transaction.class).whereEqualTo(Transaction.KEY_CIRCLE, getCurrentCircle());
        // include receiver object
        query.include(Transaction.KEY_EXPENSE);
        query.include(Transaction.KEY_RECEIVER);
        query.include(Transaction.KEY_PAYER);
        query.include(Transaction.KEY_EXPENSE + "." + Expense.KEY_CREATOR);

        // start an asynchronous call for Transaction objects
        query.findInBackground(new FindCallback<Transaction>() {
            @Override
            public void done(List<Transaction> transactions, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting expenses", e);
                    return;
                }

                // no expenses
                if(transactions.isEmpty()){
                    Log.i(TAG, "No expenses");
                }

                // clear lists
                circleTransactions.clear();
                myPendingRequests.clear();
                myPendingPayments.clear();
                myCompletedRequests.clear();
                myCompletedPayments.clear();

                // save received expenses to list
                for(int i=0; i<transactions.size(); i++){
                    Transaction t = transactions.get(i);
                    circleTransactions.add(t);

                    // transactions where current user pays money
                    if(t.getPayer().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
                        if(t.getCompleted()){
                            myCompletedPayments.add(t.getExpense());
                        }
                        else{
                            myPendingPayments.add(t.getExpense());
                        }
                    }

                    // transaction where current user receives money
                    if(t.getReceiver().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
                        if(!t.getCompleted() && !expenseExists(t.getExpense(), myPendingRequests)){
                            myPendingRequests.add(t.getExpense());
                            myCompletedRequests.removeIf(exp -> exp.getObjectId().equals(t.getExpense().getObjectId()));
                        }
                        else if(t.getCompleted()
                                && !expenseExists(t.getExpense(), myCompletedRequests)
                                && !expenseExists(t.getExpense(), myPendingRequests)){
                            myCompletedRequests.add(t.getExpense());
                        }
                    }
                }
                updateExpenseList(getFilterInt());
            }
        });
    }

    /**
     * Search for expense in expenseList
     * @param expense
     * @param expenseList
     * @return whether expense is in expenseList
     */
    public static boolean expenseExists(Expense expense, List<Expense> expenseList){
        for(int i=0; i<expenseList.size(); i++){
            if(expenseList.get(i) != null && expenseList.get(i).getObjectId().equals(expense.getObjectId())){
                return true;
            }
        }
        return false;
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
     * add Expense object on database
     * @param context
     * @param etExpenseName
     * @param etTotal
     * @param split
     * @param transactionViews
     * @param transactionUsers
     */
    public static void submitExpense(Context context,
                                     @NonNull EditText etExpenseName,
                                     EditText etTotal,
                                     boolean split,
                                     List<View> transactionViews,
                                     Bitmap bitmap,
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
        // upload receipt image if uploaded
        if(bitmap != null){
            entity.put("proof", conversionBitmapParseFile(bitmap));
        }

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
                    circleExpenses.add(expense);
                    myPendingRequests.add(expense);
                    initExpenses();
                    ((Activity) context).finish();
                }
            }else{
                //Something went wrong
                Toast.makeText(context, "Error adding expense", Toast.LENGTH_LONG).show();
                Log.e(TAG, e.getMessage());
            }
        });
    }

    /**
     * add Transaction objects for each user assigned to pay expense
     * @param context
     * @param transactionViews
     * @param transactionUsers
     * @param expense
     */
    public static void submitTransactions(Context context, List<View> transactionViews, List<ParseUser> transactionUsers, Expense expense){
        String currentUser = ParseUser.getCurrentUser().getObjectId();

        // loop through all users in circle
        for(int i=0; i<transactionViews.size(); i++){
            View view = transactionViews.get(i);
            CheckBox checkName = view.findViewById(R.id.checkName);
            int finalI = i;

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
                entity.put("circle", getCurrentCircle());

                // Saves the new object.
                // Notice that the SaveCallback is totally optional!
                entity.saveInBackground(e -> {
                    if (e==null){
                        //Save was done
                        circleTransactions.add(entity);

                        // all transactions successfully saved
                        if(finalI + 1 == transactionViews.size()){
                            Toast.makeText(context, "Added expense", Toast.LENGTH_SHORT).show();
                            circleExpenses.add(expense);
                            myPendingRequests.add(expense);
                            initExpenses();
                            ((Activity) context).finish();
                        }
                    }else{
                        //Something went wrong
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(context, "Error making transaction", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
            else if (finalI + 1 == transactionViews.size()){
                Toast.makeText(context, "Added expense", Toast.LENGTH_SHORT).show();
                circleExpenses.add(expense);
                myPendingRequests.add(expense);
                initExpenses();
                ((Activity) context).finish();
            }
        }
    }

    /**
     * @param expense
     * @param transactionList
     * @return list of all transactions in transactionList that are a part of the expense
     */
    public static List<Transaction> getAllExpenseTransactions(Expense expense, List<Transaction> transactionList){
        List<Transaction> list = new ArrayList<>();
        String searchExpense = expense.getObjectId();
        for(int i=0; i<transactionList.size(); i++){
            String transactionExpense = transactionList.get(i).getExpense().getObjectId();
            if(searchExpense.equals(transactionExpense)){
                list.add(transactionList.get(i));
            }
        }

        return list;
    }

    /**
     * @param expense
     * @param transactionList
     * @return Current user's transaction associated with expense or null if none
     */
    public static Transaction getMyExpenseTransaction(Expense expense, List<Transaction> transactionList){
        String searchExpense = expense.getObjectId();

        for(int i=0; i<transactionList.size(); i++){
            Transaction t = transactionList.get(i);
            if(searchExpense.equals(t.getExpense().getObjectId())
                    && t.getPayer().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
                return t;
            }
        }
        return null;
    }

    /**
     * mark expense as completed and reflect in card view
     * @param expense
     * @param completed
     * @param card
     */
    public static void changeTransactionStatus(Expense expense,
                                               boolean completed,
                                               com.google.android.material.card.MaterialCardView card){
        Transaction t = getMyExpenseTransaction(expense, circleTransactions);

        if(t != null){
            t.setCompleted(completed);
            t.saveInBackground(e -> {
                if(e == null){
                    // save completed
                    Log.i(TAG, "Card status set to complete " + completed);
                    if(card != null){
                        card.setChecked(!card.isChecked());
                    }
                    initExpenses();
                    updateExpenseList(getFilterInt());
                }
                else{
                    Log.e(TAG, "Error updating transaction");
                }
            });
        }
        else{
            Log.i(TAG, "No transaction found");
        }
    }

    /**
     * send reminder to all users assigned to pay expense but have not
     * @param context
     * @param expense
     */
    public static void sendReminder(Context context, Expense expense){
        List<Transaction> transactions = getAllExpenseTransactions(expense, getCircleTransactions());
        for(int i=0; i<transactions.size(); i++){
            Transaction t = transactions.get(i);
            if(!t.getCompleted()){
                sendToDeviceGroup(t.getPayer().getString("notificationKey"), "House expense reminder!", "Pending request for " + expense.getName());
            }
        }
        Toast.makeText(context, "Reminders sent!", Toast.LENGTH_SHORT).show();
    }


    public static void cancelExpense(Expense expense){
        circleExpenses.remove(expense);

        // get transactions belonging to the expense
        ParseQuery<Transaction> query = ParseQuery.getQuery(Transaction.class).whereEqualTo(Transaction.KEY_EXPENSE, expense);

        // start an asynchronous call for Transaction objects
        query.findInBackground(new FindCallback<Transaction>() {
            @Override
            public void done(List<Transaction> transactions, ParseException e) {
                if(e == null){
                    for(int i=0; i<transactions.size(); i++){
                        // delete transaction
                        int finalI = i;
                        transactions.get(i).deleteInBackground(e2 -> {
                            if(e2==null){
                                Log.i(TAG, "Success deleting transaction");
                                circleTransactions.remove(transactions.get(finalI));
                            }else{
                                //Something went wrong while deleting the Object
                                Log.e(TAG, "Error deleting transaction", e2);
                            }
                        });
                    }
                    deleteExpense(expense);
                }
            }
        });
    }

    public static void deleteExpense(Expense expense){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Expense");

        // Retrieve the object by id
        query.getInBackground(expense.getObjectId(), (object, e) -> {
            if (e == null) {
                //Object was fetched
                //Deletes the fetched ParseObject from the database
                object.deleteInBackground(e2 -> {
                    if(e2==null){
                        Log.i(TAG, "Success deleting expense");
                        circleExpenses.remove(expense);
                    }else{
                        //Something went wrong while deleting the Object
                        Log.e(TAG, "Error deleting expense", e2);
                    }
                });
            }else{
                //Something went wrong
                Log.e(TAG, "Error querying expense", e);;
            }
        });
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
     * Update Expense object
     * @param expense
     * @param name
     * @param total
     */
    public static void editExpense(Expense expense, String name, Float total, Bitmap bitmap){
        expense.setName(name);
        expense.setTotal(total);
        if(bitmap != null){
            expense.setProof(conversionBitmapParseFile(bitmap));
        }
        expense.saveInBackground(e -> {
            if (e==null){
                initExpenses();
            } }
        );
    }
}

package com.example.roomies.model;

import static com.example.roomies.ExpenseFragment.getFilterInt;
import static com.example.roomies.ExpenseFragment.updateExpenseList;
import static com.example.roomies.model.CircleManager.getCurrentCircle;
import static com.example.roomies.utils.ExpenseUtils.removeDollar;
import static com.example.roomies.utils.Utils.conversionBitmapParseFile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.roomies.R;
import com.google.android.material.card.MaterialCardView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ExpenseCollection {
    private List<Expense> circleExpenses;
    private List<Transaction> circleTransactions;

    // paid by me
    private List<Expense> myPendingPayments;
    private List<Expense> myCompletedPayments;

    // paid to me
    private List<Expense> myPendingRequests;
    private List<Expense> myCompletedRequests;

    public static final String TAG = "ExpenseCollection";

    public ExpenseCollection(){}

    public ExpenseCollection(Context context){
        initExpenses(context);
    }

    public List<Expense> getCircleExpenses(){
        return circleExpenses;
    }

    public List<Transaction> getCircleTransactions(){
        return circleTransactions;
    }

    public List<Expense> getMyCompletedPayments(){
        return myCompletedPayments;
    }

    public List<Expense> getMyPendingPayments(){
        return myPendingPayments;
    }

    public List<Expense> getMyCompletedRequests(){
        return myCompletedRequests;
    }

    public List<Expense> getMyPendingRequests(){
        return myPendingRequests;
    }

    /**
     * Initialize all lists of expenses
     */
    public void initExpenses(Context context){
        Log.i(TAG, "INIT EXPENSES");
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                initCircleExpenses(context);
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                initCircleTransactions(context);
            }
        });

        thread1.start();
        thread2.start();
    }

    /**
     * Clear all locally stored lists of expenses
     */
    public void clearAll(){
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
    public void initCircleExpenses(Context context){
        // initialize circleExpenses
        if(circleExpenses == null){
            circleExpenses = new ArrayList<>();
        }

        // no current circle
        if(getCurrentCircle() == null){
            return;
        }

        // only get expenses from user's current circle
        ParseQuery<Expense> query = ParseQuery.getQuery(Expense.class).whereEqualTo(Chore.KEY_CIRCLE, getCurrentCircle());
        // include receiver object
        query.include(Expense.KEY_CREATOR);
        query.orderByDescending(Expense.KEY_CREATED_AT);
        // start an asynchronous call for Expense objects
        // call from local datastore then network
        query.fromLocalDatastore().findInBackground().continueWithTask((task) -> {
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting expenses", e);
            }
            else{
                Log.i(TAG, "Retrieved expenses locally");
                setExpenses(task.getResult());
            }
            return query.fromNetwork().findInBackground();
        }, ContextCompat.getMainExecutor(context)).continueWithTask((task) -> {
            // Update UI with results from Network ...
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting expenses", e);
            }
            else{
                Log.i(TAG, "Retrieved expenses from network");
                setExpenses(task.getResult());
                Expense.pinAllInBackground(task.getResult());
            }
            return task;
        }, ContextCompat.getMainExecutor(context));;
    }

    private void setExpenses(List<Expense> expenses){
        // no expenses
        if(expenses.isEmpty()){
            Log.i(TAG, "No circle expenses");
        }

        // save received expenses to list and notify adapter of new data
        circleExpenses.clear();
        circleExpenses.addAll(expenses);
        updateExpenseList(getFilterInt());
    }

    /**
     * Query transactions belonging to current circle
     */
    public void initCircleTransactions(Context context){
        if(circleTransactions == null){ circleTransactions = new ArrayList<>(); }
        if(myCompletedPayments == null){ myCompletedPayments = new ArrayList<>(); }
        if(myPendingPayments == null){ myPendingPayments = new ArrayList<>(); }
        if(myPendingRequests == null){ myPendingRequests = new ArrayList<>(); }
        if(myCompletedRequests == null){ myCompletedRequests = new ArrayList<>(); }

        // no current circle
        if(getCurrentCircle() == null){
            return;
        }

        // only get expenses from user's current circle
        ParseQuery<Transaction> query = ParseQuery.getQuery(Transaction.class).whereEqualTo(Transaction.KEY_CIRCLE, getCurrentCircle());
        // include receiver object
        query.include(Transaction.KEY_EXPENSE);
        query.include(Transaction.KEY_RECEIVER);
        query.include(Transaction.KEY_PAYER);
        query.include(Transaction.KEY_EXPENSE + "." + Expense.KEY_CREATOR);

        query.orderByDescending(Transaction.KEY_CREATED_AT);

        // start an asynchronous call for Transaction objects
        // query from local datastore then network
        query.fromLocalDatastore().findInBackground().continueWithTask((task) -> {
            ParseException e = (ParseException) task.getError();
            if(e != null){
                Log.e(TAG, "Issue with getting expenses locally", e);
            }
            else{
                setTransactions(task.getResult());
            }
            // Now query the network:
            return query.fromNetwork().findInBackground();
        }, ContextCompat.getMainExecutor(context)).continueWithTask((task) -> {
            // Update UI with results from Network ...
            ParseException e = (ParseException) task.getError();
            if(e != null){
                Log.e(TAG, "Issue with getting expenses from network", e);
            }
            else{
                setTransactions(task.getResult());
                Transaction.pinAllInBackground(task.getResult());
            }
            return task;
        }, ContextCompat.getMainExecutor(context));
    }

    private void setTransactions(List<Transaction> transactions){
        // no expenses
        if(transactions.isEmpty()){
            Log.i(TAG, "No expenses");
        }
        else{
            Log.i(TAG, "has transactions " + transactions);
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
     * add Expense object on database
     * @param context
     * @param etExpenseName
     * @param etTotal
     * @param split
     * @param transactionViews
     * @param transactionUsers
     */
    public void submitExpense(Context context,
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
        entity.put("circle", getCurrentCircle());
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
                    initExpenses(context);
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
    private void submitTransactions(Context context, List<View> transactionViews, List<ParseUser> transactionUsers, Expense expense){
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
                            initExpenses(context);
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
                initExpenses(context);
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
    public void changeTransactionStatus(Context context,
                                               Expense expense,
                                               boolean completed,
                                               MaterialCardView card){
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
                    initExpenses(context);
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

    public void cancelExpense(Expense expense){
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

    private void deleteExpense(Expense expense){
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
     * Update Expense object
     * @param expense
     * @param name
     * @param total
     */
    public void editExpense(Context context, Expense expense, String name, Float total, Bitmap bitmap){
        expense.setName(name);
        expense.setTotal(total);
        if(bitmap != null){
            expense.setProof(conversionBitmapParseFile(bitmap));
        }
        expense.saveInBackground(e -> {
            if (e==null){
                initExpenses(context);
            } }
        );
    }
}

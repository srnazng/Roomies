package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Transaction")
public class Transaction extends ParseObject {
    public static final String KEY_RECEIVER = "receiver";
    public static final String KEY_PAYER = "payer";
    public static final String KEY_EXPENSE = "expense";
    public static final String KEY_COMPLETED = "completed";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_CIRCLE = "circle";

    public Expense getExpense() { return (Expense) getParseObject(KEY_EXPENSE); }

    public void setExpense(Expense expense) { put(KEY_EXPENSE, expense); }

    public boolean getCompleted() { return getBoolean(KEY_COMPLETED); }

    public void setCompleted(boolean completed) { put(KEY_COMPLETED, completed); }

    public ParseUser getPayer() { return getParseUser(KEY_PAYER); }

    public void setPayer(ParseUser user) { put(KEY_PAYER, user); }

    public ParseUser getReceiver() { return getParseUser(KEY_RECEIVER); }

    public double getAmount() { return getDouble(KEY_AMOUNT); }
}

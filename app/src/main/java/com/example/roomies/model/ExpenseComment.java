package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("ExpenseComment")
public class ExpenseComment extends ParseObject {
    public static final String KEY_EXPENSE = "expense";
    public static final String KEY_USER = "user";
    public static final String KEY_COMMENT = "comment";

    public Expense getExpense() { return (Expense) getParseObject(KEY_EXPENSE); }

    public void setExpense(Expense expense) { put(KEY_EXPENSE, expense); }

    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }

    public String getComment() { return getString(KEY_COMMENT); }

    public void setComment( String comment ) { put(KEY_COMMENT, comment); }

}

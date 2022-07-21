package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("ChoreAssignment")
public class ChoreAssignment extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String KEY_CHORE = "chore";
    public static final String KEY_CIRCLE = "circle";

    public Chore getChore(){ return (Chore) getParseObject(KEY_CHORE); }

    public void setChore(Chore chore){ put(KEY_CHORE, chore); }

    public ParseUser getUser(){ return getParseUser(KEY_USER); }

    public void setUser(ParseUser user) { put(KEY_USER, user); }

    public Circle getCircle() { return (Circle) getParseObject(KEY_CIRCLE); }

    public void setCircle (Circle circle) { put(KEY_CIRCLE, circle); }
}

package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("UserCircle")
public class UserCircle extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String KEY_CIRCLE = "circle";
    public static final String KEY_POINTS = "points";

    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }

    public Circle getCircle(){
        return (Circle) getParseObject(KEY_CIRCLE);
    }

    public void setCircle(Circle circle) { put(KEY_CIRCLE, circle); }

    public int getPoints() { return getInt(KEY_POINTS); }

    public void addPoints( int num ) { put(KEY_POINTS, getPoints() + num ); }
}

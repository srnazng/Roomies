package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("UserCircle")
public class UserCircle extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String KEY_CIRCLE = "circle";

    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }

    public Circle getCircle(){
        return (Circle) getParseObject(KEY_CIRCLE);
    }
}

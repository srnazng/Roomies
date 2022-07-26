package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("GroceryItem")
public class GroceryItem extends ParseObject {
    public static final String KEY_NAME = "name";
    public static final String KEY_CIRCLE = "circle";
    public static final String KEY_COMPLETED = "completed";

    public String getName() { return getString(KEY_NAME); }

    public void setName(String name) { put(KEY_NAME, name); }

    public Circle getCircle() { return (Circle) getParseObject(KEY_CIRCLE); }

    public void setCircle(Circle circle) { put(KEY_CIRCLE, circle); }

    public boolean getCompleted() { return getBoolean(KEY_COMPLETED); }

    public void setCompleted( boolean completed ) { put(KEY_COMPLETED, completed); }
}

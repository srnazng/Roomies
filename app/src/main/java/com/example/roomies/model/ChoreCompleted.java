package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("ChoreCompleted")
public class ChoreCompleted extends ParseObject {
    public static final String KEY_CHORE_ASSIGNMENT = "choreAssignment";
    public static final String KEY_COMPLETED = "completed";
    public static final String KEY_CIRCLE = "circle";

    public ChoreAssignment getChoreAssignment() { return (ChoreAssignment) getParseObject(KEY_CHORE_ASSIGNMENT); }

    public boolean getCompleted() { return getBoolean(KEY_COMPLETED); }

    public void setCompleted(boolean completed) { put(KEY_COMPLETED, completed); }
}

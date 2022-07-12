package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("ChoreCompleted")
public class ChoreCompleted extends ParseObject {
    public static final String KEY_CHORE_ASSIGNMENT = "choreAssignment";
    public static final String KEY_COMPLETED = "completed";
}

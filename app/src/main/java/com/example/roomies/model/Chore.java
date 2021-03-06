package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Chore")
public class Chore extends ParseObject {
    public static final String KEY_CREATOR = "creator";
    public static final String KEY_CIRCLE = "circle";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_POINTS = "points";
    public static final String KEY_ALL_DAY = "allDay";
    public static final String KEY_DUE = "dueDatetime";
    public static final String KEY_RECURRENCE = "recurrence";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_EDITOR = "lastEditedBy";

    public static final String PRIORITY_LOW = "Low";
    public static final String PRIORITY_MED = "Medium";
    public static final String PRIORITY_HIGH = "High";

    public ParseUser getCreator(){
        return getParseUser(KEY_CREATOR);
    }

    public void setCreator(ParseUser user){
        put(KEY_CREATOR, user);
    }

    public Circle getCircle(){
        return (Circle) getParseObject(KEY_CIRCLE);
    }

    public void setCircle(Circle circle) { put(KEY_CIRCLE, circle); }

    public String getTitle() { return getString(KEY_TITLE); }

    public void setTitle(String title) { put(KEY_TITLE, title); }

    public String getDescription() { return getString(KEY_DESCRIPTION); }

    public void setDescription(String description) {put(KEY_DESCRIPTION, description); }

    public String getPriority() { return getString(KEY_PRIORITY); }

    public void setPriorityLow() { put(KEY_TITLE, PRIORITY_LOW); }

    public void setPriorityMed() { put(KEY_TITLE, PRIORITY_MED); }

    public void setPriorityHigh() { put(KEY_TITLE, PRIORITY_HIGH); }

    public int getPoints() { return getInt(KEY_POINTS); }

    public void setPoints(int points) { put(KEY_POINTS, points); }

    public Boolean getAllDay() { return getBoolean(KEY_ALL_DAY); }

    public void setAllDay(Boolean allDay) { put(KEY_ALL_DAY, allDay); }

    public Date getDue() { return getDate(KEY_DUE); }

    public void setDue(Date date) { put(KEY_DUE, date); }

    public Recurrence getRecurrence() { return (Recurrence) getParseObject(KEY_RECURRENCE); }

    public void setRecurrence(Recurrence recurrence) { put(KEY_RECURRENCE, recurrence); }

    public int getDuration() { return getInt(KEY_DURATION); }

    public void setDuration( int duration ) { put(KEY_DURATION, duration); }

    public ParseUser getLastEditedBy() { return getParseUser(KEY_EDITOR); }

    public void setLastEditedBy(ParseUser user) { put(KEY_EDITOR, user); }
}

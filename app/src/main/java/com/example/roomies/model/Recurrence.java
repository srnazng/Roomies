package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;

import java.util.Date;
import java.util.List;

@ParseClassName("Recurrence")
public class Recurrence extends ParseObject {
    public static final String KEY_CHORE = "chore";
    public static final String KEY_END_DATE = "endDate";
    public static final String KEY_FREQUENCY = "frequency";
    public static final String KEY_FREQUENCY_TYPE = "frequencyType";
    public static final String KEY_DAYS_OF_WEEK = "daysOfWeek";
    public static final String KEY_NUM_OCCURRENCES = "numOccurrences";

    public static final String TYPE_DAY = "day";
    public static final String TYPE_WEEK = "week";
    public static final String TYPE_MONTH = "month";
    public static final String TYPE_YEAR = "year";

    public enum DaysOfWeek { SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY};

    public Chore getChore(){  return (Chore) getParseObject(KEY_CHORE); }

    public void setChore(Chore chore){ put(KEY_CHORE, chore); }

    public Date getEndDate() { return getDate(KEY_END_DATE); }

    public void setEndDate( Date endDate ) { put(KEY_END_DATE, endDate); }

    public Integer getFrequency() { return getInt(KEY_FREQUENCY); }

    public void setFrequency(Integer frequency) { put(KEY_FREQUENCY, frequency); }

    public String getFrequencyType() { return getString(KEY_FREQUENCY_TYPE); }

    public void setFrequencyType(String frequencyType){ put(KEY_FREQUENCY_TYPE, frequencyType); }

    public String getDaysOfWeek() { return getString(KEY_DAYS_OF_WEEK); }

    public void setDaysOfWeek( String days) { put(KEY_DAYS_OF_WEEK, days); }

    public int getNumOccurrences() { return getInt(KEY_NUM_OCCURRENCES); }

    public void setNumOccurrences(int numOccurrences) { put(KEY_NUM_OCCURRENCES, numOccurrences); }
}

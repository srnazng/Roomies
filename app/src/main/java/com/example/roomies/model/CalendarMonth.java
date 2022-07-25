package com.example.roomies.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CalendarMonth{
    private List<CalendarDay> days;
    private int month;
    private int year;
    private boolean myChoresOnly;

    public CalendarMonth(int year, int month, boolean myChoresOnly){
        this.month = month;
        this.year = year;
        this.days = new ArrayList<>();
        this.myChoresOnly = myChoresOnly;
    }

    public List<CalendarDay> getDays() {
        return days;
    }

    public void setDays(List<CalendarDay> days) {
        this.days = days;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}

package com.example.roomies.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarDay {
    private Calendar day;
    private List<Chore> chores;

    public CalendarDay(Calendar day){
        this.day = day;
        chores = new ArrayList<>();
    }

    public Calendar getDay(){ return day; }

    public void setDay(Calendar c){ day = c; }

    public List<Chore> getChores(){ return chores; }

    public void setChores(List<Chore> c){
        chores.clear();
        chores.addAll(c);
    }
}

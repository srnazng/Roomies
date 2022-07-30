package com.example.roomies;

import static com.example.roomies.utils.CalendarDayUtils.occursToday_dayFreq;
import static com.example.roomies.utils.CalendarDayUtils.occursToday_monthFreq;
import static com.example.roomies.utils.CalendarDayUtils.occursToday_weekFreq;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Calendar;

public class CalendarTest {

    @Test
    public void testOccursToday(){
        Calendar c = Calendar.getInstance();
        assertEquals(true, occursToday_dayFreq(Calendar.getInstance(), 1, c));
        assertEquals(true, occursToday_weekFreq(Calendar.getInstance(), "0,1,2,3,4,5,6", 1, c));
        assertEquals(false, occursToday_weekFreq(Calendar.getInstance(), "", 1, c));
        assertEquals(true, occursToday_monthFreq(Calendar.getInstance(), 1, c));

        c.add(Calendar.DAY_OF_YEAR, 1); // 1 day after
        assertEquals(false, occursToday_dayFreq(Calendar.getInstance(), 2, c));
        assertEquals(true, occursToday_weekFreq(Calendar.getInstance(), "0,1,2,3,4,5,6", 1, c));

        c.add(Calendar.DAY_OF_YEAR, -2); // 1 day before
        assertEquals(false, occursToday_dayFreq(Calendar.getInstance(), 1, c));
        assertEquals(false, occursToday_weekFreq(Calendar.getInstance(), "0,1,2,3,4,5,6", 1, c));
        assertEquals(false, occursToday_monthFreq(Calendar.getInstance(), 1, c));

        c.add(Calendar.DAY_OF_YEAR, 1); // today
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        String daysOfWeek = dayOfWeek + ",";
        assertEquals(true, occursToday_weekFreq(Calendar.getInstance(), daysOfWeek, 1, c));

        c.add(Calendar.MONTH, 1);
        assertEquals(false, occursToday_monthFreq(Calendar.getInstance(), 2, c));
    }
}

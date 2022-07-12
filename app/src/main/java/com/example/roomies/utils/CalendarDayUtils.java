package com.example.roomies.utils;

import static com.example.roomies.utils.Utils.clearTime;
import static com.example.roomies.utils.Utils.compareDates;
import static com.example.roomies.utils.Utils.occursToday_dayFreq;
import static com.example.roomies.utils.Utils.occursToday_monthFreq;
import static com.example.roomies.utils.Utils.occursToday_weekFreq;

import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.adapter.CalendarAdapter;
import com.example.roomies.model.CalendarDay;
import com.example.roomies.model.Chore;
import com.example.roomies.model.Recurrence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarDayUtils {
    private static List<CalendarDay> calendarList;
    private static Calendar firstOfMonth;

    public static final String TAG = "CalendarDayUtils";

    public static Calendar getFirstOfMonth(){
        if(firstOfMonth == null){
            firstOfMonth = Calendar.getInstance();
            firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        }
        return firstOfMonth;
    }

    public static void setFirstOfMonth(Calendar c){
        firstOfMonth = c;
    }

    public static List<CalendarDay> getCalendarList(){
        return calendarList;
    }

    public static void updateAdapter(CalendarAdapter adapter, RecyclerView rvCalendar){
        adapter.notifyDataSetChanged();

        // scroll to today's date
        Calendar today = Calendar.getInstance();

        if(today.get(Calendar.MONTH) == firstOfMonth.get(Calendar.MONTH) &&
                today.get(Calendar.YEAR) == firstOfMonth.get(Calendar.YEAR) &&
                adapter.getItemCount() > today.get(Calendar.DAY_OF_MONTH) - 1 ){
            rvCalendar.post(new Runnable() {
                @Override
                public void run() {
                    rvCalendar.smoothScrollToPosition(today.get(Calendar.DAY_OF_MONTH) - 1);
                }
            });
        }
        else{
            rvCalendar.post(new Runnable() {
                @Override
                public void run() {
                    rvCalendar.smoothScrollToPosition(0);
                }
            });
        }
    }

    public static void updateCalendar(List<Chore> chores,
                                      Calendar begin,
                                      CalendarAdapter adapter,
                                      RecyclerView rvCalendar){

        // start of month
        Calendar start = Calendar.getInstance();
        start.setTime(begin.getTime());
        clearTime(start);

        // end of month
        Calendar end = Calendar.getInstance();
        end.setTime(start.getTime());
        end.add(Calendar.DAY_OF_YEAR, begin.getActualMaximum(Calendar.DAY_OF_MONTH));
        clearTime(end);

        initCalendar();

        // add myChores to chores lists of CalendarDay objects
        for(int i=0; i<chores.size(); i++){
            Chore c = chores.get(i);
            Calendar due = Calendar.getInstance();
            due.setTime(c.getDue());
            clearTime(due);

            // add if due date specified as a day in this month
            if(compareDates(due, start) >= 0 && compareDates(end, due) > 0){
                for(int j=0; j<begin.getActualMaximum(Calendar.DAY_OF_MONTH); j++){
                    if(compareDates(due, calendarList.get(j).getDay()) == 0){
                        calendarList.get(j).getChores().add(c);
                        updateAdapter(adapter, rvCalendar);
                        continue;
                    }
                }
            }

            // add if recurrence occurs in this month
            Recurrence r = c.getRecurrence();
            Calendar endRecurrenceDate = Calendar.getInstance();

            if(r != null){
                endRecurrenceDate.setTime(r.getEndDate());
                clearTime(endRecurrenceDate);
            }

            // if recurrence has not ended
            if(r != null && compareDates(endRecurrenceDate, start) >= 0){
                Calendar startRecurrenceDate = Calendar.getInstance();
                startRecurrenceDate.setTime(c.getDue());
                clearTime(startRecurrenceDate);

                // recurrence with daily/weekly frequency
                if(r.getFrequencyType().equals(Recurrence.TYPE_DAY) ||
                        r.getFrequencyType().equals(Recurrence.TYPE_WEEK)){
                    for(int j=0; j<firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH); j++){
                        Calendar thisDay = calendarList.get(j).getDay();
                        // if calendar day is before endDate of recurrence
                        if(compareDates(thisDay, endRecurrenceDate) <= 0 ){
                            // daily recurrence
                            if(r.getFrequencyType().equals(Recurrence.TYPE_DAY)
                                    && compareDates(thisDay, startRecurrenceDate) >= 0
                                    && occursToday_dayFreq(startRecurrenceDate, r.getFrequency(), thisDay)
                                    && !calendarList.get(j).getChores().contains(c)){
                                calendarList.get(j).getChores().add(c);
                            }
                            // weekly recurrence
                            else if(r.getFrequencyType().equals(Recurrence.TYPE_WEEK)
                                    && compareDates(thisDay, startRecurrenceDate) >= 0
                                    && occursToday_weekFreq(startRecurrenceDate, r.getDaysOfWeek(), r.getFrequency(), thisDay)
                                    && !calendarList.get(j).getChores().contains(c)){
                                calendarList.get(j).getChores().add(c);
                            }
                        }
                        else { break; }
                    }
                }
                // recurrence with monthly frequency
                else if(r.getFrequencyType().equals(Recurrence.TYPE_MONTH)){
                    int dayOfMonth = due.get(Calendar.DAY_OF_MONTH);
                    if(firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH) >= dayOfMonth){
                        Calendar thisDay = calendarList.get(dayOfMonth - 1).getDay();
                        if(compareDates(thisDay, endRecurrenceDate) <= 0
                                && compareDates(thisDay, startRecurrenceDate) >= 0
                                && occursToday_monthFreq(startRecurrenceDate, r.getFrequency(), thisDay)
                                && !calendarList.get(dayOfMonth - 1).getChores().contains(c)){
                            calendarList.get(dayOfMonth - 1).getChores().add(c);
                        }
                    }
                }
                // recurrence with yearly frequency
                else {
                    if(firstOfMonth.get(Calendar.MONTH) == due.get(Calendar.MONTH)
                            && (firstOfMonth.get(Calendar.YEAR) - due.get(Calendar.YEAR)) % r.getFrequency() == 0){
                        int dayOfMonth = due.get(Calendar.DAY_OF_MONTH);
                        Calendar thisDay = calendarList.get(dayOfMonth - 1).getDay();
                        if(compareDates(thisDay, endRecurrenceDate) <= 0
                                && compareDates(thisDay, startRecurrenceDate) >= 0
                                && !calendarList.get(dayOfMonth - 1).getChores().contains(c)){
                            calendarList.get(dayOfMonth - 1).getChores().add(c);
                        }
                    }
                }
            }
        }
        updateAdapter(adapter, rvCalendar);
    }

    // create CalendarDay objects for entire month
    public static void initCalendar(){
        if(firstOfMonth == null){
            firstOfMonth = Calendar.getInstance();
            firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        }
        if(calendarList == null){
            calendarList = new ArrayList<>();
        }

        calendarList.clear();

        for(int i=0; i<firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH); i++){
            Calendar date = Calendar.getInstance();
            date.setTime(firstOfMonth.getTime());
            date.add(Calendar.DAY_OF_MONTH, i);

            CalendarDay day = new CalendarDay(date);
            day.setChores(new ArrayList<>());
            calendarList.add(day);
        }
    }
}

package com.example.roomies.utils;

import static com.example.roomies.utils.Utils.clearTime;
import static com.example.roomies.utils.Utils.compareDates;
import static com.example.roomies.utils.Utils.getDaysDifference;
import static com.example.roomies.utils.Utils.getWeeksDifference;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.adapter.CalendarAdapter;
import com.example.roomies.model.CalendarDay;
import com.example.roomies.model.CalendarMonth;
import com.example.roomies.model.Chore;
import com.example.roomies.model.Recurrence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarDayUtils {
    private static List<CalendarDay> calendarList;
    private static Calendar firstOfMonth;
    private static List<CalendarMonth> calendarCache;
    private static List<CalendarMonth> circleCalendarCache;

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

    public static void refreshCalendar(List<Chore> chores,
                                       Calendar begin,
                                       CalendarAdapter adapter,
                                       boolean myChoresOnly,
                                       RecyclerView rvCalendar){

        if(calendarCache != null){
            calendarCache.clear();
        }

        if(circleCalendarCache != null){
            circleCalendarCache.clear();
        }

        // start of month
        Calendar start = Calendar.getInstance();
        start.setTime(begin.getTime());
        clearTime(start);

        // end of month
        Calendar end = Calendar.getInstance();
        end.setTime(start.getTime());
        end.add(Calendar.DAY_OF_YEAR, begin.getActualMaximum(Calendar.DAY_OF_MONTH));
        clearTime(end);

        // update calendar month
        initCalendar();

        // add myChores to chores lists of CalendarDay objects
        for(int i=0; i<chores.size(); i++) {
            Chore c = chores.get(i);
            Calendar due = Calendar.getInstance();
            due.setTime(c.getDue());
            clearTime(due);

            // add if due date specified as a day in this month
            if (compareDates(due, start) >= 0 && compareDates(end, due) > 0) {
                for (int j = 0; j < begin.getActualMaximum(Calendar.DAY_OF_MONTH); j++) {
                    if (compareDates(due, calendarList.get(j).getDay()) == 0) {
                        calendarList.get(j).getChores().add(c);
                        updateAdapter(adapter, rvCalendar);
                        continue;
                    }
                }
            }

            // add if recurrence occurs in this month
            Recurrence r = c.getRecurrence();
            Calendar endRecurrenceDate = Calendar.getInstance();

            if (r != null) {
                endRecurrenceDate.setTime(r.getEndDate());
                clearTime(endRecurrenceDate);
            }

            // if recurrence has not ended
            if (r != null && compareDates(endRecurrenceDate, start) >= 0) {
                Calendar startRecurrenceDate = Calendar.getInstance();
                startRecurrenceDate.setTime(c.getDue());
                clearTime(startRecurrenceDate);

                // recurrence with daily/weekly frequency
                if (r.getFrequencyType().equals(Recurrence.TYPE_DAY) ||
                        r.getFrequencyType().equals(Recurrence.TYPE_WEEK)) {
                    for (int j = 0; j < firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH); j++) {
                        Calendar thisDay = calendarList.get(j).getDay();
                        // if calendar day is before endDate of recurrence
                        if (compareDates(thisDay, endRecurrenceDate) <= 0) {
                            // daily recurrence
                            if (r.getFrequencyType().equals(Recurrence.TYPE_DAY)
                                    && compareDates(thisDay, startRecurrenceDate) >= 0
                                    && occursToday_dayFreq(startRecurrenceDate, r.getFrequency(), thisDay)
                                    && !calendarList.get(j).getChores().contains(c)) {
                                calendarList.get(j).getChores().add(c);
                            }
                            // weekly recurrence
                            else if (r.getFrequencyType().equals(Recurrence.TYPE_WEEK)
                                    && compareDates(thisDay, startRecurrenceDate) >= 0
                                    && occursToday_weekFreq(startRecurrenceDate, r.getDaysOfWeek(), r.getFrequency(), thisDay)
                                    && !calendarList.get(j).getChores().contains(c)) {
                                calendarList.get(j).getChores().add(c);
                            }
                        } else {
                            break;
                        }
                    }
                }
                // recurrence with monthly frequency
                else if (r.getFrequencyType().equals(Recurrence.TYPE_MONTH)) {
                    int dayOfMonth = due.get(Calendar.DAY_OF_MONTH);
                    if (firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH) >= dayOfMonth) {
                        Calendar thisDay = calendarList.get(dayOfMonth - 1).getDay();
                        if (compareDates(thisDay, endRecurrenceDate) <= 0
                                && compareDates(thisDay, startRecurrenceDate) >= 0
                                && occursToday_monthFreq(startRecurrenceDate, r.getFrequency(), thisDay)
                                && !calendarList.get(dayOfMonth - 1).getChores().contains(c)) {
                            calendarList.get(dayOfMonth - 1).getChores().add(c);
                        }
                    }
                }
                // recurrence with yearly frequency
                else {
                    if (firstOfMonth.get(Calendar.MONTH) == due.get(Calendar.MONTH)
                            && (firstOfMonth.get(Calendar.YEAR) - due.get(Calendar.YEAR)) % r.getFrequency() == 0) {
                        int dayOfMonth = due.get(Calendar.DAY_OF_MONTH);
                        Calendar thisDay = calendarList.get(dayOfMonth - 1).getDay();
                        if (compareDates(thisDay, endRecurrenceDate) <= 0
                                && compareDates(thisDay, startRecurrenceDate) >= 0
                                && !calendarList.get(dayOfMonth - 1).getChores().contains(c)) {
                            calendarList.get(dayOfMonth - 1).getChores().add(c);
                        }
                    }
                }
            }
        }

            // save to list of CalendarMonths
            int year = firstOfMonth.get(Calendar.YEAR);
            int month = firstOfMonth.get(Calendar.MONTH);
            if(inCacheRange(year, month)){
                if(myChoresOnly && calendarCache == null){
                    calendarCache = new ArrayList<>();
                }
                else if(!myChoresOnly && circleCalendarCache == null){
                    circleCalendarCache = new ArrayList<>();
                }
                CalendarMonth calendarMonth = getCalendarMonth(firstOfMonth.get(Calendar.YEAR), firstOfMonth.get(Calendar.MONTH), myChoresOnly);
                if(calendarMonth == null){
                    calendarMonth = new CalendarMonth(year, month, myChoresOnly);
                    List<CalendarDay> list = new ArrayList<>();
                    list.addAll(calendarList);
                    calendarMonth.setDays(list);
                    if(myChoresOnly) {
                        calendarCache.add(calendarMonth);
                    }
                    else{
                        circleCalendarCache.add(calendarMonth);
                    }
                }
                else{
                    calendarMonth.getDays().clear();
                    calendarMonth.getDays().addAll(calendarList);
                }
            }

            // update recycler view
            updateAdapter(adapter, rvCalendar);
    }

    public static void updateCalendar(List<Chore> chores,
                                      Calendar begin,
                                      CalendarAdapter adapter,
                                      boolean myChoresOnly,
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

        boolean fromCache = false;

        initCalendar();

        // first get month's events from stored CalendarMonth object
        if(inCacheRange(firstOfMonth.get(Calendar.YEAR),
                firstOfMonth.get(Calendar.MONTH))){
            CalendarMonth m = getCalendarMonth(firstOfMonth.get(Calendar.YEAR),
                    firstOfMonth.get(Calendar.MONTH), myChoresOnly);
            if(m != null){
                calendarList.clear();
                calendarList.addAll(m.getDays());
                adapter.notifyDataSetChanged();
                fromCache = true;
            }
        }

        // update calendar month
        if(!fromCache){
            initCalendar();

            Log.e(TAG, "UPDATE CALENDAR FROM SCRATCH");

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

            // save to list of CalendarMonths
            int year = firstOfMonth.get(Calendar.YEAR);
            int month = firstOfMonth.get(Calendar.MONTH);
            if(inCacheRange(year, month)){
                if(myChoresOnly && calendarCache == null){
                    calendarCache = new ArrayList<>();
                }
                else if(!myChoresOnly && circleCalendarCache == null){
                    circleCalendarCache = new ArrayList<>();
                }
                CalendarMonth calendarMonth = getCalendarMonth(firstOfMonth.get(Calendar.YEAR), firstOfMonth.get(Calendar.MONTH), myChoresOnly);
                if(calendarMonth == null){
                    calendarMonth = new CalendarMonth(year, month, myChoresOnly);
                    List<CalendarDay> list = new ArrayList<>();
                    list.addAll(calendarList);
                    calendarMonth.setDays(list);
                    if(myChoresOnly) {
                        calendarCache.add(calendarMonth);
                    }
                    else{
                        circleCalendarCache.add(calendarMonth);
                    }
                }
                else{
                    calendarMonth.getDays().clear();
                    calendarMonth.getDays().addAll(calendarList);
                }
            }

            // update recycler view
            updateAdapter(adapter, rvCalendar);
        }
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

    /**
     * Get CalendarMonth object from stored Months
     * @param year
     * @param month
     * @return
     */
    public static CalendarMonth getCalendarMonth(int year, int month, boolean myChoresOnly){
        if(myChoresOnly){
            if(calendarCache == null){
                return null;
            }

            Log.i(TAG, "get calendar month " + calendarCache.toString());

            boolean clean = false;

            if(calendarCache.size() > 5){
                clean = true;
            }

            for(int i=0; i<calendarCache.size(); i++){
                CalendarMonth m = calendarCache.get(i);
                if(m.getYear() == year && m.getMonth() == month){
                    return m;
                }
                if(clean && !inCacheRange(year, month)){
                    calendarCache.remove(m);
                }
            }
        }
        else{
            if(circleCalendarCache == null){
                return null;
            }

            Log.i(TAG, circleCalendarCache.toString());

            boolean clean = false;

            if(circleCalendarCache.size() > 5){
                clean = true;
            }

            for(int i=0; i<circleCalendarCache.size(); i++){
                CalendarMonth m = circleCalendarCache.get(i);
                if(m.getYear() == year && m.getMonth() == month){
                    return m;
                }
                if(clean && !inCacheRange(year, month)){
                    circleCalendarCache.remove(m);
                }
            }
        }

        return null;
    }

    /**
     * Whether month is stored
     * @param year
     * @param month
     * @return
     */
    public static boolean inCacheRange(int year, int month){
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, 1);
        clearTime(today);

        int diffYear = year - today.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + month - today.get(Calendar.MONTH);

        if(diffMonth >= 2 || diffMonth <= -2){
            return false;
        }
        return true;
    }

    /**
     * Clear stored CalendarMonths
     */
    public static void clearCalendarCache(){
        if(calendarCache != null){
            calendarCache.clear();
        }
    }

    /**
     * determines if an event with daily recurrence occurs on a day
     * @param startRecurrence   Start of recurrence
     * @param freq              Frequency of days event occurs on
     * @param today             Date being evaluated
     * @return  whether the recurring event occurs on given date
     */
    public static boolean occursToday_dayFreq(Calendar startRecurrence, int freq, Calendar today){
        clearTime(startRecurrence);
        clearTime(today);

        if(today.compareTo(startRecurrence) < 0){
            return false;
        }

        int diff = getDaysDifference(startRecurrence.getTime(), today.getTime());

        if(diff % freq == 0){
            return true;
        }
        return false;
    }

    /**
     * determines if an event with weekly recurrence occurs on a day
     * @param startRecurrence   Start of recurrence
     * @param daysOfWeek        Days of week event occurs on
     * @param freq              Frequency of weeks event occurs
     * @param today             Date being evaluated
     * @return  whether the recurring event occurs on given date
     */
    public static boolean occursToday_weekFreq(Calendar startRecurrence, String daysOfWeek, int freq, Calendar today){
        clearTime(startRecurrence);
        clearTime(today);

        if(today.compareTo(startRecurrence) < 0){
            return false;
        }

        long diff = getWeeksDifference(startRecurrence, today);
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK) - 1;

        if(!daysOfWeek.contains(dayOfWeek + ",")){
            return false;
        }

        if(diff % freq != 0){
            return false;
        }

        return true;
    }

    /**
     * determines if an event with monthly recurrence occurs this month
     * @param startRecurrence   Start of recurrence
     * @param freq              Frequency of months event occurs on
     * @param today             Date being evaluated
     * @return whether the recurring event occurs on given date
     */
    public static boolean occursToday_monthFreq(Calendar startRecurrence, int freq, Calendar today){
        clearTime(startRecurrence);
        clearTime(today);

        if(today.compareTo(startRecurrence) < 0){
            return false;
        }

        long diff = Utils.getMonthsDifference(startRecurrence, today);
        if(diff % freq == 0){
            return true;
        }

        return false;
    }

}

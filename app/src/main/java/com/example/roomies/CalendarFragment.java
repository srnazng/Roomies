package com.example.roomies;

import static com.example.roomies.HomeFragment.currentCircle;
import static com.example.roomies.utils.Utils.clearTime;
import static com.example.roomies.utils.Utils.getDaysDifference;
import static com.example.roomies.utils.Utils.getMonthForInt;
import static com.example.roomies.utils.Utils.getWeeksDifference;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomies.adapter.CalendarAdapter;
import com.example.roomies.model.CalendarDay;
import com.example.roomies.model.Chore;
import com.example.roomies.model.ChoreAssignment;
import com.example.roomies.model.Recurrence;
import com.example.roomies.utils.Utils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Chore calendar fragment
 */
public class CalendarFragment extends Fragment {
    private RecyclerView rvCalendar;
    private CalendarAdapter adapter;
    private List<CalendarDay> calendar;
    private TextView tvMonth;
    private ImageView ivPrevMonth;
    private ImageView ivNextMonth;

    private List<Chore> allChores;
    private List<Chore> myChores;
    private Calendar firstOfMonth;

    private boolean showAll = false;

    public static final String TAG = "CalendarFragment";

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment CalendarFragment.
     */

    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Lookup the calendar recyclerview
        rvCalendar = view.findViewById(R.id.rvCalendar);

        // Initialize
        myChores = new ArrayList<>();
        allChores = new ArrayList<>();
        calendar = new ArrayList<>();
        updateMyChores();

        // Create adapter
        adapter = new CalendarAdapter(calendar);
        // Attach the adapter to the recyclerview to populate items
        rvCalendar.setAdapter(adapter);
        // Set layout manager to position the items
        rvCalendar.setLayoutManager(new LinearLayoutManager(getActivity()));

        // month currently being displayed
        firstOfMonth = Calendar.getInstance();
        firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        tvMonth = view.findViewById(R.id.tvMonth);
        tvMonth.setText(getMonthForInt(firstOfMonth.get(Calendar.MONTH)) + " " + firstOfMonth.get(Calendar.YEAR));

        // button to go to next month
        ivNextMonth = view.findViewById(R.id.ivNextMonth);
        ivNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstOfMonth.add(Calendar.MONTH, 1);
                tvMonth.setText(getMonthForInt(firstOfMonth.get(Calendar.MONTH)) + " " + firstOfMonth.get(Calendar.YEAR));
                updateCalendar(myChores, firstOfMonth);
            }
        });

        // button to go to previous month
        ivPrevMonth = view.findViewById(R.id.ivPrevMonth);
        ivPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstOfMonth.add(Calendar.MONTH, -1);
                tvMonth.setText(getMonthForInt(firstOfMonth.get(Calendar.MONTH)) + " " + firstOfMonth.get(Calendar.YEAR));
                updateCalendar(myChores, firstOfMonth);
            }
        });
        return view;
    }

    // get chores that are assigned to current user
    public void updateMyChores(){
        // only get chore assignments of current user
        ParseQuery<ChoreAssignment> query = ParseQuery.getQuery(ChoreAssignment.class).whereEqualTo(ChoreAssignment.KEY_USER, ParseUser.getCurrentUser());
        // include chore and recurrence objects
        query.include(ChoreAssignment.KEY_CHORE);
        query.include(ChoreAssignment.KEY_CHORE + "." + Chore.KEY_RECURRENCE);
        // start an asynchronous call for ChoreAssignment objects
        query.findInBackground(new FindCallback<ChoreAssignment>() {
            @Override
            public void done(List<ChoreAssignment> choreAssignments, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting choreAssignments", e);
                    Toast.makeText(getActivity(), "Unable to retrieve chores", Toast.LENGTH_SHORT).show();
                    return;
                }

                // no chores
                if(choreAssignments == null || choreAssignments.isEmpty()){
                    Log.i(TAG, "No assigned chores");
                }
                else{
                    // save received chore assignments in this circle to list
                    myChores.clear();
                    for(int i=0; i<choreAssignments.size(); i++) {
                        ChoreAssignment c = choreAssignments.get(i);
                        if(c.getChore().getCircle().getObjectId().equals(currentCircle.getObjectId())){
                            myChores.add(c.getChore());
                        }
                    }

                    // create each day item in calendar
                    updateCalendar(myChores, firstOfMonth);
                }
            }
        });
    }

    // create CalendarDay objects for recyclerview
    public void updateCalendar(List<Chore> chores, Calendar begin){
        calendar.clear();

        // start of month
        Calendar start = Calendar.getInstance();
        start.setTime(begin.getTime());
        clearTime(start);

        // end of month
        Calendar end = Calendar.getInstance();
        end.setTime(start.getTime());
        end.add(Calendar.DAY_OF_YEAR, begin.getActualMaximum(Calendar.DAY_OF_MONTH));
        clearTime(end);

        // create CalendarDay objects for entire month
        for(int i=0; i<begin.getActualMaximum(Calendar.DAY_OF_MONTH); i++){
            Calendar date = Calendar.getInstance();
            date.setTime(start.getTime());
            date.add(Calendar.DAY_OF_MONTH, i);

            CalendarDay day = new CalendarDay(date);
            day.setChores(new ArrayList<>());
            calendar.add(day);
        }

        // add myChores to chores lists of CalendarDay objects
        for(int i=0; i<chores.size(); i++){
            Chore c = chores.get(i);
            Calendar due = Calendar.getInstance();
            due.setTime(c.getDue());
            clearTime(due);

            // add if due date specified as a day in this month
            if(compare(due, start) >= 0 && compare(end, due) > 0){
                for(int j=0; j<begin.getActualMaximum(Calendar.DAY_OF_MONTH); j++){
                    if(compare(due, calendar.get(j).getDay()) == 0){
                        calendar.get(j).getChores().add(c);
                        updateAdapter();
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
            if(r != null && compare(endRecurrenceDate, start) >= 0){
                Calendar startRecurrenceDate = Calendar.getInstance();
                startRecurrenceDate.setTime(c.getDue());
                clearTime(startRecurrenceDate);

                // recurrence with daily/weekly frequency
                if(r.getFrequencyType().equals(Recurrence.TYPE_DAY) ||
                        r.getFrequencyType().equals(Recurrence.TYPE_WEEK)){
                    for(int j=0; j<firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH); j++){
                        Calendar thisDay = calendar.get(j).getDay();
                        // if calendar day is before endDate of recurrence
                        if(compare(thisDay, endRecurrenceDate) <= 0 ){
                            // daily recurrence
                            if(r.getFrequencyType().equals(Recurrence.TYPE_DAY)
                                    && compare(thisDay, startRecurrenceDate) >= 0
                                    && occursToday_dayFreq(startRecurrenceDate, r.getFrequency(), thisDay)
                                    && !calendar.get(j).getChores().contains(c)){
                                calendar.get(j).getChores().add(c);
                            }
                            // weekly recurrence
                            else if(r.getFrequencyType().equals(Recurrence.TYPE_WEEK)
                                    && compare(thisDay, startRecurrenceDate) >= 0
                                    && occursToday_weekFreq(startRecurrenceDate, r.getDaysOfWeek(), r.getFrequency(), thisDay)
                                    && !calendar.get(j).getChores().contains(c)){
                                calendar.get(j).getChores().add(c);
                            }
                        }
                        else { break; }
                    }
                }
                // recurrence with monthly frequency
                else if(r.getFrequencyType().equals(Recurrence.TYPE_MONTH)){
                    int dayOfMonth = due.get(Calendar.DAY_OF_MONTH);

                    if(firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH) >= dayOfMonth){
                        Calendar thisDay = calendar.get(dayOfMonth - 1).getDay();
                        if(compare(thisDay, endRecurrenceDate) <= 0
                                && compare(thisDay, startRecurrenceDate) >= 0
                                && occursToday_monthFreq(startRecurrenceDate, r.getFrequency(), thisDay)
                                && !calendar.get(dayOfMonth - 1).getChores().contains(c)){
                            calendar.get(dayOfMonth - 1).getChores().add(c);
                        }
                    }
                }
                // recurrence with yearly frequency
                else {
                    if(firstOfMonth.get(Calendar.MONTH) == due.get(Calendar.MONTH)
                         && (firstOfMonth.get(Calendar.YEAR) - due.get(Calendar.YEAR)) % r.getFrequency() == 0){
                        int dayOfMonth = due.get(Calendar.DAY_OF_MONTH);
                        Calendar thisDay = calendar.get(dayOfMonth - 1).getDay();
                        if(compare(thisDay, endRecurrenceDate) <= 0
                                && compare(thisDay, startRecurrenceDate) >= 0
                                && !calendar.get(dayOfMonth - 1).getChores().contains(c)){
                            calendar.get(dayOfMonth - 1).getChores().add(c);
                        }
                    }
                }
                updateAdapter();
            }
        }
    }

    public void updateAdapter(){
        adapter.notifyDataSetChanged();

        // scroll to today's date
        Calendar today = Calendar.getInstance();
        if(today.get(Calendar.MONTH) == firstOfMonth.get(Calendar.MONTH) &&
                today.get(Calendar.YEAR) == firstOfMonth.get(Calendar.YEAR) &&
                adapter.getItemCount() > today.get(Calendar.DAY_OF_MONTH) ){
            rvCalendar.post(new Runnable() {
                @Override
                public void run() {
                    rvCalendar.smoothScrollToPosition(today.get(Calendar.DAY_OF_MONTH));
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

    // compare calendar objects ignoring time
    public int compare(Calendar c1, Calendar c2) {
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
            return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
        if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
            return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
        return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
    }

    // determines if an event occurs on a day given a starting day and a repetition frequency
    public boolean occursToday_dayFreq(Calendar startRecurrence, int freq, Calendar today){
        clearTime(startRecurrence);
        clearTime(today);

        int diff = getDaysDifference(startRecurrence.getTime(), today.getTime());

        if(diff % freq == 0){
            return true;
        }
        return false;
    }

    // determines if an event occurs on a day given a starting day, selected days of week, and a repetition frequency
    public boolean occursToday_weekFreq(Calendar startRecurrence, String daysOfWeek, int freq, Calendar today){
        clearTime(startRecurrence);
        clearTime(today);

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

    public boolean occursToday_monthFreq(Calendar startRecurrence, int freq, Calendar today){
        clearTime(startRecurrence);
        clearTime(today);

        long diff = Utils.getMonthsDifference(startRecurrence, today);

        if(diff % freq == 0){
            return true;
        }
        return false;
    }
}
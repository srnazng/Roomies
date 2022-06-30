package com.example.roomies;

import static com.example.roomies.utils.TimeUtils.*;

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
import com.example.roomies.model.Recurrence;
import com.example.roomies.utils.ChoreUtils;

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

        // set calendar month
        firstOfMonth = Calendar.getInstance();
        firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);

        // Initialize
        myChores = ChoreUtils.getMyChores();
        allChores = ChoreUtils.getCircleChores();
        calendar = new ArrayList<>();

        // Create adapter
        adapter = new CalendarAdapter(calendar);
        // Attach the adapter to the recyclerview to populate items
        rvCalendar.setAdapter(adapter);
        // Set layout manager to position the items
        rvCalendar.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateMyChores();

        // month currently being displayed
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
        if(ChoreUtils.getMyChores() != null){
            // create each day item in calendar
            updateCalendar(ChoreUtils.getMyChores(), firstOfMonth);
        }
        else if(myChores.isEmpty()){
            Toast.makeText(getActivity(), "No chores today!", Toast.LENGTH_SHORT).show();
        }
        else { Log.e(TAG, "Error retrieving chores"); }
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

        initCalendar(start);

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

    // create CalendarDay objects for entire month
    public void initCalendar(Calendar start){
        for(int i=0; i<start.getActualMaximum(Calendar.DAY_OF_MONTH); i++){
            Calendar date = Calendar.getInstance();
            date.setTime(start.getTime());
            date.add(Calendar.DAY_OF_MONTH, i);

            CalendarDay day = new CalendarDay(date);
            day.setChores(new ArrayList<>());
            calendar.add(day);
        }
    }

}
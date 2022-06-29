package com.example.roomies;

import static com.example.roomies.HomeFragment.currentCircle;
import static com.example.roomies.utils.Utils.getMonthForInt;

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
import com.example.roomies.model.UserCircle;
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
        query.include(Chore.KEY_RECURRENCE);
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
                        if(c.getChore().getCircle().equals(currentCircle)){
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

        start.clear(Calendar.HOUR);
        start.clear(Calendar.MINUTE);
        start.clear(Calendar.SECOND);
        start.add(Calendar.SECOND, -1);

        // end of month
        Calendar end = Calendar.getInstance();
        end.setTime(start.getTime());
        end.add(Calendar.DAY_OF_YEAR, begin.getActualMaximum(Calendar.DAY_OF_MONTH));

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

            // add if due date specified as day
            if(c.getDue().after(start.getTime()) && c.getDue().before(end.getTime())){
                for(int j=0; j<begin.getActualMaximum(Calendar.DAY_OF_MONTH); j++){
                    if(due.get(Calendar.DAY_OF_MONTH) ==
                            calendar.get(j).getDay().get(Calendar.DAY_OF_MONTH)){
                        calendar.get(j).getChores().add(c);
                        break;
                    }
                }
            }
            else if(c.getRecurrence() != null){
                // TODO: determine if chore is today based on recurrence
            }
        }
        adapter.notifyDataSetChanged();
    }
}
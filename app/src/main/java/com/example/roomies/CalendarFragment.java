package com.example.roomies;

import static com.example.roomies.utils.CalendarDayUtils.*;
import static com.example.roomies.utils.Utils.*;

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
import com.example.roomies.model.Chore;
import com.example.roomies.utils.ChoreUtils;

import java.util.Calendar;
import java.util.List;

/**
 * Chore calendar fragment
 */
public class CalendarFragment extends Fragment {
    private RecyclerView rvCalendar;
    private CalendarAdapter adapter;

    private TextView tvMonth;
    private ImageView ivPrevMonth;
    private ImageView ivNextMonth;

    private List<Chore> allChores;
    private List<Chore> myChores;

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
        myChores = ChoreUtils.getMyChores();
        allChores = ChoreUtils.getCircleChores();

        // Create adapter
        adapter = new CalendarAdapter(getCalendarList());
        // Attach the adapter to the recyclerview to populate items
        rvCalendar.setAdapter(adapter);
        // Set layout manager to position the items
        rvCalendar.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateMyChores();

        // month currently being displayed
        tvMonth = view.findViewById(R.id.tvMonth);
        tvMonth.setText(getMonthForInt(getFirstOfMonth().get(Calendar.MONTH)) + " " + getFirstOfMonth().get(Calendar.YEAR));

        // button to go to next month
        ivNextMonth = view.findViewById(R.id.ivNextMonth);
        ivNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFirstOfMonth().add(Calendar.MONTH, 1);
                tvMonth.setText(getMonthForInt(getFirstOfMonth().get(Calendar.MONTH)) + " " + getFirstOfMonth().get(Calendar.YEAR));
                updateCalendar(getActivity(), myChores, getFirstOfMonth(), adapter, rvCalendar);
            }
        });

        // button to go to previous month
        ivPrevMonth = view.findViewById(R.id.ivPrevMonth);
        ivPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFirstOfMonth().add(Calendar.MONTH, -1);
                tvMonth.setText(getMonthForInt(getFirstOfMonth().get(Calendar.MONTH)) + " " + getFirstOfMonth().get(Calendar.YEAR));
                updateCalendar(getActivity(), myChores, getFirstOfMonth(), adapter, rvCalendar);
            }
        });
        return view;
    }

    // get chores that are assigned to current user
    public void updateMyChores(){
        if(ChoreUtils.getMyChores() != null){
            // create each day item in calendar
            updateCalendar(getActivity(), ChoreUtils.getMyChores(), getFirstOfMonth(), adapter ,rvCalendar);
        }
        else if(myChores.isEmpty()){
            Toast.makeText(getActivity(), "No chores today!", Toast.LENGTH_SHORT).show();
        }
        else { Log.e(TAG, "Error retrieving chores"); }
    }

}
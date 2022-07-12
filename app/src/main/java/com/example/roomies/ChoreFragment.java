package com.example.roomies;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.roomies.adapter.ChoreAdapter;
import com.example.roomies.model.Chore;
import com.example.roomies.utils.ChoreUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for chore list and related actions
 */
public class ChoreFragment extends Fragment {

    private com.google.android.material.floatingactionbutton.FloatingActionButton btnToCalendar;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnAddChore;
    private RecyclerView rvChores;
    private ChoreAdapter adapter;

    public static List<Chore> choreList;

    public static final String TAG = "ChoreFragment";

    public ChoreFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment ChoreFragment.
     */
    public static ChoreFragment newInstance() {
        ChoreFragment fragment = new ChoreFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize chores
        choreList = ChoreUtils.getMyChoresToday();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chore, container, false);

        // Bind to recycler view
        rvChores = view.findViewById(R.id.rvChores);

        // Create adapter passing in the chore data
        adapter = new ChoreAdapter(choreList);
        updateChoreList();

        // Attach the adapter to the recyclerview to populate items
        rvChores.setAdapter(adapter);
        // Set layout manager to position the items
        rvChores.setLayoutManager(new LinearLayoutManager(getActivity()));

        // button to calendar fragment
        btnToCalendar = view.findViewById(R.id.btnToCalendar);
        btnToCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toCalendar();
            }
        });

        // button to add chore
        btnAddChore = view.findViewById(R.id.btnAdd);
        btnAddChore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddChoreActivity.class);
                startActivity(i);
            }
        });
        return view;
    }

    // update chore list whenever screen resumes
    @Override
    public void onResume() {
        super.onResume();
        ChoreUtils.initChores();
        updateChoreList();
    }

    // go to calendar page
    public void toCalendar() {
        FragmentTransaction fragmentTransaction = (getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, CalendarFragment.newInstance());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // query database for circle's list of chores
    public void updateChoreList(){
        choreList = ChoreUtils.getMyChoresToday();
        adapter.notifyDataSetChanged();
    }
}
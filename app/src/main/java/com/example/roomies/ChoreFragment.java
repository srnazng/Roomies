package com.example.roomies;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Fragment for chore list and related actions
 */
public class ChoreFragment extends Fragment {

    private Button btnToCalendar;
    private Button btnAddChore;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chore, container, false);

        // button to calendar fragment
        btnToCalendar = view.findViewById(R.id.btnToCalendar);
        btnToCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toCalendar();
            }
        });

        // button to add chore
        btnAddChore = view.findViewById(R.id.btnAddChore);
        btnAddChore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddChoreActivity.class);
                startActivity(i);
            }
        });
        return view;
    }

    // go to calendar page
    public void toCalendar() {
        FragmentTransaction fragmentTransaction = (getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, CalendarFragment.newInstance());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
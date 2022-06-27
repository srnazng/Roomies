package com.example.roomies;

import static com.example.roomies.HomeFragment.currentCircle;

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
import android.widget.Toast;

import com.example.roomies.adapter.ChoreAdapter;
import com.example.roomies.model.Chore;
import com.example.roomies.model.UserCircle;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for chore list and related actions
 */
public class ChoreFragment extends Fragment {

    private Button btnToCalendar;
    private Button btnAddChore;
    private RecyclerView rvChores;
    private ChoreAdapter adapter;

    public List<Chore> choreList;

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
        choreList = new ArrayList<>();
        updateChoreList();
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
        // only get chores for user's current circle
        ParseQuery<Chore> query = ParseQuery.getQuery(Chore.class).whereEqualTo(Chore.KEY_CIRCLE, currentCircle);
        // include data referred by user key
        query.include(UserCircle.KEY_USER);
        // start an asynchronous call for Chore objects
        query.findInBackground(new FindCallback<Chore>() {
            @Override
            public void done(List<Chore> chores, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting userCircles", e);
                    Toast.makeText(getActivity(), "Unable to retrieve chores", Toast.LENGTH_SHORT).show();
                    return;
                }

                // no chores
                if(chores.isEmpty()){
                    Toast.makeText(getActivity(), "No chores today!", Toast.LENGTH_SHORT).show();
                }

                // save received chores to list and notify adapter of new data
                choreList.clear();
                choreList.addAll(chores);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
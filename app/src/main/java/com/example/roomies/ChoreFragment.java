package com.example.roomies;

import static com.example.roomies.utils.ChoreUtils.markCompleted;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.roomies.adapter.ChoreAdapter;
import com.example.roomies.model.Chore;
import com.example.roomies.utils.ChoreUtils;
import com.example.roomies.utils.SwipeToDeleteCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * Fragment for chore list and related actions
 */
public class ChoreFragment extends Fragment {

    private FloatingActionButton btnToCalendar;
    private FloatingActionButton btnAddChore;
    private RecyclerView rvChores;
    private ChoreAdapter adapter;
    private ConstraintLayout choreListLayout;

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
        choreList = ChoreUtils.getMyPendingChoresToday();
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

        // Set swipe to delete for list
        choreListLayout = view.findViewById(R.id.choreListLayout);
        setSwipeToDelete();

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
        choreList = ChoreUtils.getMyPendingChoresToday();
        adapter.notifyDataSetChanged();
    }

    // attach SwipeToDeleteCallback
    // define what happens on swipe or undo
    private void setSwipeToDelete(){
        // set swipe to delete
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(getActivity()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final Chore item = adapter.getData().get(position);
                markCompleted(item, true);
                adapter.removeItem(position);

                Snackbar snackbar = Snackbar
                        .make(choreListLayout, "Chore marked completed.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        markCompleted(item, false);
                        adapter.restoreItem(item, position);
                        rvChores.scrollToPosition(position);
                    }
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(rvChores);
    }
}
package com.example.roomies;

import static com.example.roomies.utils.ChoreUtils.initChores;
import static com.example.roomies.utils.ChoreUtils.markCompleted;

import android.animation.Animator;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.example.roomies.adapter.ChoreAdapter;
import com.example.roomies.adapter.CompletedChoreAdapter;
import com.example.roomies.model.Chore;
import com.example.roomies.utils.ChoreUtils;
import com.example.roomies.utils.SwipeToDeleteCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;

/**
 * Fragment for chore list and related actions
 */
public class ChoreFragment extends Fragment {
    private FloatingActionButton btnToCalendar;
    private FloatingActionButton btnAddChore;
    private RecyclerView rvChores;
    private RecyclerView rvCompletedChores;
    private static ChoreAdapter adapter;
    private static CompletedChoreAdapter completedAdapter;
    private ConstraintLayout choreListLayout;
    private SwipeRefreshLayout choreSwipeContainer;

    private static LottieAnimationView checkAnimation;

    public static List<Chore> choreList;
    public static List<Chore> completedChoreList;
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
        completedChoreList = ChoreUtils.getMyCompletedChoresToday();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chore, container, false);

        checkAnimation = view.findViewById(R.id.checkAnimation);
        checkAnimation.setVisibility(View.GONE);

        // Bind to recycler view
        rvChores = view.findViewById(R.id.rvChores);
        rvCompletedChores = view.findViewById(R.id.rvDone);

        // Create adapter passing in the chore data
        adapter = new ChoreAdapter(choreList);
        completedAdapter = new CompletedChoreAdapter(completedChoreList);
        updateChoreList();

        // Attach the adapter to the recyclerview to populate items
        rvChores.setAdapter(adapter);
        rvCompletedChores.setAdapter(completedAdapter);
        // Set layout manager to position the items
        rvChores.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCompletedChores.setLayoutManager(new LinearLayoutManager(getActivity()));

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

        // pull down to refresh
        choreSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.choreSwipeContainer);
        // Setup refresh listener which triggers new data loading
        choreSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // refresh chore list
                initChores(getActivity());
                choreSwipeContainer.setRefreshing(false);
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
    public static void updateChoreList(){
        if(adapter == null || completedAdapter == null){
            return;
        }

        choreList = ChoreUtils.getMyPendingChoresToday();
        adapter.notifyDataSetChanged();

        completedChoreList = ChoreUtils.getMyCompletedChoresToday();
        completedAdapter.notifyDataSetChanged();
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
                markCompleted(getActivity(), item, true, Calendar.getInstance());
                markCompleteAdapter(position, item);
                showCheck();

                Snackbar snackbar = Snackbar
                        .make(choreListLayout, "Chore marked completed.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        markCompleted(getActivity(), item, false, Calendar.getInstance());
                        markIncompleteAdapter(position, item);
                        rvChores.scrollToPosition(position);
                        updateChoreList();
                    }
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
                updateChoreList();
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(rvChores);
    }

    public static void markCompleteAdapter(int position, Chore item){
        adapter.removeItem(position);
        completedAdapter.restoreItem(item, completedAdapter.getItemCount());
    }

    public static void markIncompleteAdapter(int position, Chore item){
        adapter.restoreItem(item, position);
        completedAdapter.removeItem(completedAdapter.getItemCount() - 1);
    }

    // show check mark animation
    public static void showCheck(){
        checkAnimation.setVisibility(View.VISIBLE);
        checkAnimation.setProgress(0);
        checkAnimation.pauseAnimation();
        checkAnimation.playAnimation();
        checkAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { checkAnimation.setVisibility(View.VISIBLE); }

            @Override
            public void onAnimationEnd(Animator animation) { checkAnimation.setVisibility(View.GONE);}

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }
}
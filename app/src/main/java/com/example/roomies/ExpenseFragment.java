package com.example.roomies;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.example.roomies.adapter.ExpenseAdapter;
import com.example.roomies.model.Expense;
import com.example.roomies.utils.ExpenseUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * House expenses fragment
 */
public class ExpenseFragment extends Fragment {
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnAddExpense;
    private RecyclerView rvExpenses;
    private static ExpenseAdapter adapter;
    private static Spinner expenseType;
    private static CheckBox checkPending;
    private static CheckBox checkCompleted;
    private static ConstraintLayout layoutFilter;

    private static Filters filter;
    public enum Filters {MY_PAYMENTS, MY_REQUESTS, CIRCLE_EXPENSES};

    public static final String TAG = "ExpenseFragment";

    private static List<Expense> expenseList;

    public ExpenseFragment() {
        // Required empty public constructor
    }

    public static Filters getFilter(){
        return Filters.values()[expenseType.getSelectedItemPosition()];
    }

    public static int getFilterInt(){
        if(expenseType == null){
            return 0;
        }
        return Filters.values()[expenseType.getSelectedItemPosition()].ordinal();
    }

    /**
     * @return A new instance of fragment ExpenseFragment.
     */
    public static ExpenseFragment newInstance() {
        ExpenseFragment fragment = new ExpenseFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expenseList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        // Lookup the recyclerview in activity layout
        rvExpenses = view.findViewById(R.id.rvExpenses);
        // Create adapter passing in the expense list
        adapter = new ExpenseAdapter(expenseList);
        // Attach the adapter to the recyclerview to populate items
        rvExpenses.setAdapter(adapter);
        // Set layout manager to position the items
        rvExpenses.setLayoutManager(new LinearLayoutManager(getActivity()));

        btnAddExpense = view.findViewById(R.id.btnAddExpense);
        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddExpenseActivity.class);
                startActivity(i);
            }
        });

        layoutFilter = view.findViewById(R.id.layoutFilter);
        layoutFilter.setVisibility(View.VISIBLE);
        checkPending = view.findViewById(R.id.checkPending);
        checkPending.setChecked(true);
        checkPending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateExpenseList(expenseType.getSelectedItemPosition());
            }
        });
        checkCompleted = view.findViewById(R.id.checkCompleted);
        checkCompleted.setChecked(true);
        checkCompleted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateExpenseList(expenseType.getSelectedItemPosition());
            }
        });

        expenseType = view.findViewById(R.id.expenseType);
        expenseType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateExpenseList(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                expenseType.setSelection(0);
                filter = Filters.values()[expenseType.getSelectedItemPosition()];
            }
        });

        updateExpenseList(expenseType.getSelectedItemPosition());

        return view;
    }

    // update expense list on fragment resume
    @Override
    public void onResume() {
        super.onResume();
        updateExpenseList(expenseType.getSelectedItemPosition());
    }

    // query database for circle's expenses
    public static void updateExpenseList(int position){
        if(layoutFilter == null){
            return;
        }

        if(expenseList != null){
            expenseList.clear();
        }
        else{
            return;
        }

        if(adapter != null){
            adapter.clear();
        }

        if(position == 0){
            layoutFilter.setVisibility(View.VISIBLE);
            if(checkPending.isChecked() && ExpenseUtils.getMyPendingPayments() != null){
                expenseList.addAll(ExpenseUtils.getMyPendingPayments());
            }
            if(checkCompleted.isChecked() && ExpenseUtils.getMyCompletedPayments() != null){
                expenseList.addAll(ExpenseUtils.getMyCompletedPayments());
            }
        }
        else if(position == 1){
            layoutFilter.setVisibility(View.VISIBLE);
            if(checkPending.isChecked() && ExpenseUtils.getMyPendingRequests() != null){
                expenseList.addAll(ExpenseUtils.getMyPendingRequests());
            }
            if(checkCompleted.isChecked() && ExpenseUtils.getMyCompletedRequests() != null){
                expenseList.addAll(ExpenseUtils.getMyCompletedRequests());
            }
        }
        else if(ExpenseUtils.getCircleExpenses() != null){
            layoutFilter.setVisibility(View.INVISIBLE);
            expenseList.addAll(ExpenseUtils.getCircleExpenses());
        }

        filter = Filters.values()[expenseType.getSelectedItemPosition()];

        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }
}
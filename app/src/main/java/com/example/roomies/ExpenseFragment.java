package com.example.roomies;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.roomies.adapter.ExpenseAdapter;
import com.example.roomies.model.Expense;
import com.example.roomies.utils.ExpenseUtils;

import java.util.List;

/**
 * House expenses fragment
 */
public class ExpenseFragment extends Fragment {
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnAddExpense;
    private RecyclerView rvExpenses;
    private ExpenseAdapter adapter;

    public static final String TAG = "ExpenseFragment";

    private List<Expense> expenseList;

    public ExpenseFragment() {
        // Required empty public constructor
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
        expenseList = ExpenseUtils.getCircleExpenses();
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
        updateExpenseList();

        btnAddExpense = view.findViewById(R.id.btnAddExpense);
        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddExpenseActivity.class);
                startActivity(i);
            }
        });
        return view;
    }

    // update expense list on fragment resume
    @Override
    public void onResume() {
        super.onResume();
        ExpenseUtils.initExpenses();
        updateExpenseList();
    }

    // query database for circle's expenses
    public void updateExpenseList(){
        expenseList = ExpenseUtils.getCircleExpenses();
        adapter.notifyDataSetChanged();
    }
}
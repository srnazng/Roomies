package com.example.roomies;

import static com.example.roomies.HomeFragment.currentCircle;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.roomies.adapter.ExpenseAdapter;
import com.example.roomies.model.Chore;
import com.example.roomies.model.Expense;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * House expenses fragment
 */
public class ExpenseFragment extends Fragment {
    private Button btnAddExpense;
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
        expenseList = new ArrayList<>();
        updateExpenseList();
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
        return view;
    }

    // update expense list on fragment resume
    @Override
    public void onResume() {
        super.onResume();
        updateExpenseList();
    }

    // query database for circle's expenses
    public void updateExpenseList(){
        // only get expenses from user's current circle
        ParseQuery<Expense> query = ParseQuery.getQuery(Expense.class).whereEqualTo(Chore.KEY_CIRCLE, currentCircle);
        // include receiver object
        query.include(Expense.KEY_CREATOR);
        // start an asynchronous call for Expense objects
        query.findInBackground(new FindCallback<Expense>() {
            @Override
            public void done(List<Expense> expenses, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting expenses", e);
                    Toast.makeText(getActivity(), "Unable to retrieve expenses", Toast.LENGTH_SHORT).show();
                    return;
                }

                // no expenses
                if(expenses.isEmpty()){
                    Toast.makeText(getActivity(), "No expenses today!", Toast.LENGTH_SHORT).show();
                }

                // save received expenses to list and notify adapter of new data
                expenseList.clear();
                expenseList.addAll(expenses);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
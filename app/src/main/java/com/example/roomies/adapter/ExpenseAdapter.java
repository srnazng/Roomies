package com.example.roomies.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.R;
import com.example.roomies.model.Chore;
import com.example.roomies.model.Expense;
import com.parse.ParseUser;

import java.util.List;

public class ExpenseAdapter extends
        RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> expenses;

    // Pass in the chore array into the constructor
    public ExpenseAdapter(List<Expense> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View expenseView = inflater.inflate(R.layout.item_expense, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(expenseView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView tvExpenseName;
        public TextView tvPayTo;
        public TextView tvTotal;
        public com.google.android.material.card.MaterialCardView card;
        public Button btnComment;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tvExpenseName = itemView.findViewById(R.id.tvExpenseName);
            tvPayTo = itemView.findViewById(R.id.tvPayTo);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            card = itemView.findViewById(R.id.expenseCard);
            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    card.setChecked(!card.isChecked());
                    return true;
                }
            });
        }

        public void bind(Expense expense){
            tvExpenseName.setText(expense.getName());
            ParseUser receiver = expense.getCreator();
            tvPayTo.setText(receiver.getString("name"));
            tvTotal.setText(expense.getTotal().toString());
        }
    }
}


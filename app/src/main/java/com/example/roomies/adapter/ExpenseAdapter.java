package com.example.roomies.adapter;
import static com.example.roomies.utils.ExpenseUtils.cancelExpense;
import static com.example.roomies.utils.ExpenseUtils.changeTransactionStatus;
import static com.example.roomies.utils.ExpenseUtils.getAllExpenseTransactions;
import static com.example.roomies.utils.ExpenseUtils.getCircleTransactions;
import static com.example.roomies.utils.ExpenseUtils.getMyExpenseTransaction;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.EditExpenseActivity;
import com.example.roomies.ExpenseDetailActivity;
import com.example.roomies.ExpenseFragment;
import com.example.roomies.R;
import com.example.roomies.model.Expense;
import com.example.roomies.model.Transaction;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.parse.ParseUser;

import java.util.List;

public class ExpenseAdapter extends
        RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> expenses;
    private Context context;

    // Pass in the expense array into the constructor
    public ExpenseAdapter(List<Expense> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
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
        if(expenses == null){
            return 0;
        }
        return expenses.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvExpenseName;
        private TextView tvPayTo;
        private TextView tvTotal;
        private com.google.android.material.card.MaterialCardView card;
        private Button btnComment;
        private Button btnMarkPaid;
        private Button btnEdit;
        private Button btnCancel;
        private ChipGroup assigneeChips;
        private HorizontalScrollView chipScroll;

        // Constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            super(itemView);

            // bind layout items
            tvExpenseName = itemView.findViewById(R.id.tvExpenseName);
            tvPayTo = itemView.findViewById(R.id.tvPayTo);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            card = itemView.findViewById(R.id.expenseCard);
            assigneeChips = itemView.findViewById(R.id.assigneeChips);
            chipScroll = itemView.findViewById(R.id.chipScroll);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnMarkPaid = itemView.findViewById(R.id.btnMarkPaid);
            btnEdit = itemView.findViewById(R.id.btnEditExpense);
            btnCancel = itemView.findViewById(R.id.btnCancelTransaction);
            btnComment = itemView.findViewById(R.id.btnComment);
        }

        // bind expense
        public void bind(Expense expense){
            if(expense == null){
                return;
            }

            // expense title
            tvExpenseName.setText(expense.getName() + " $" + String.format("%.2f", expense.getTotal()));

            // user who created expense
            ParseUser receiver = expense.getCreator();

            // see card details on click
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ExpenseDetailActivity.class);
                    i.putExtra("expense", expense);
                    context.startActivity(i);
                }
            });

            // go to card details to comment
            btnComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ExpenseDetailActivity.class);
                    i.putExtra("expense", expense);
                    context.startActivity(i);
                }
            });

            // filter set to all circle expenses
            if(ExpenseFragment.getFilter() == ExpenseFragment.Filters.CIRCLE_EXPENSES){
                // set card subtitle
                tvPayTo.setText("Created by " + receiver.getString("name"));

                // card settings
                card.setChecked(true);
                card.setLongClickable(false);

                // determine if card should be marked completed
                List<Transaction> transactions = getAllExpenseTransactions(expense, getCircleTransactions());
                for(int i=0; i<transactions.size(); i++){
                    if(!transactions.get(i).getCompleted()){
                        card.setChecked(false);
                        break;
                    }
                }

                // set custom view visibilities
                btnCancel.setVisibility(View.GONE);
                btnMarkPaid.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                tvTotal.setVisibility(View.GONE);

                // initialize chips of users assigned to pay expense
                initializeChips(transactions);
                chipScroll.setVisibility(View.VISIBLE);
            }
            // filter set to all expense current user created
            else if(ExpenseFragment.getFilter() == ExpenseFragment.Filters.MY_REQUESTS){
                // set card subtitle
                tvPayTo.setText("Created by " + receiver.getString("name"));

                // card settings
                card.setLongClickable(false);
                card.setChecked(true);

                // determine if card should be marked completed
                List<Transaction> transactions = getAllExpenseTransactions(expense, getCircleTransactions());

                for(int i=0; i<transactions.size(); i++){
                    if(!transactions.get(i).getCompleted()){
                        card.setChecked(false);
                        break;
                    }
                }

                // set custom view visibilities
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelExpense(expense);
                        expenses.remove(expense);
                        notifyDataSetChanged();
                    }
                });
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, EditExpenseActivity.class);
                        i.putExtra("expense", expense);
                        context.startActivity(i);
                    }
                });
                btnMarkPaid.setVisibility(View.GONE);
                tvTotal.setVisibility(View.GONE);

                // initialize chips of users assigned to pay expense
                initializeChips(transactions);
                chipScroll.setVisibility(View.VISIBLE);
            }
            // filter set to all expense current user is assigned to pay
            else{
                // card settings
                card.setCheckable(true);
                card.setLongClickable(true);
                card.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        changeTransactionStatus(expense, !card.isChecked(), card);
                        return true;
                    }
                });

                // determine if card should be marked completed
                Transaction transaction = getMyExpenseTransaction(expense, getCircleTransactions());
                if(transaction != null){
                    card.setChecked(transaction.getCompleted());
                }
                else{
                    return;
                }

                // set card body
                tvPayTo.setText("Pay to " + receiver.getString("name"));
                tvTotal.setText("You owe: $" + String.format("%.2f", transaction.getAmount()));

                // set custom visibilities
                tvTotal.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.GONE);
                btnMarkPaid.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
                btnMarkPaid.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeTransactionStatus(expense, !card.isChecked(), card);
                    }
                });
                chipScroll.setVisibility(View.GONE);
            }
        }

        // set chips to all users in list of transactions
        public void initializeChips(List<Transaction> transactions){
            if(assigneeChips == null){
                return;
            }

            // clear ChipGroup
            assigneeChips.removeAllViews();

            // create and add new chips
            for(int i=0; i<transactions.size(); i++){
                Chip chip = new Chip(context);
                Transaction t = transactions.get(i);
                chip.setText(t.getPayer().getString("name"));
                chip.setClickable(false);
                chip.setCheckable(true);
                chip.setChecked(t.getCompleted());
                assigneeChips.addView(chip);
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        if(expenses != null){
            expenses.clear();
            notifyDataSetChanged();
        }
    }
}


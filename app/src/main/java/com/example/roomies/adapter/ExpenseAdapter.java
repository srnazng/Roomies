package com.example.roomies.adapter;
import static com.example.roomies.model.CircleManager.getExpenseCollection;
import static com.example.roomies.model.ExpenseCollection.getAllExpenseTransactions;
import static com.example.roomies.model.ExpenseCollection.getMyExpenseTransaction;
import static com.example.roomies.utils.ExpenseUtils.sendReminder;

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
import com.google.android.material.card.MaterialCardView;
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
        private MaterialCardView card;
        private Button btnComment;
        private Button btnMarkPaid;
        private Button btnEdit;
        private Button btnRemind;
        private Button btnCancel;
        private ChipGroup assigneeChips;
        private HorizontalScrollView chipScroll;

        private Expense expense;
        private List<Transaction> transactions;

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
            btnRemind = itemView.findViewById(R.id.btnRemind);
        }

        // bind expense
        public void bind(Expense expense){
            if(expense == null){
                return;
            }

            this.expense = expense;

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
                if(transactions != null){
                    transactions.clear();
                }
                transactions = getAllExpenseTransactions(expense, getExpenseCollection().getCircleTransactions());
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
                btnRemind.setVisibility(View.GONE);
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
                List<Transaction> transactions = getAllExpenseTransactions(expense, getExpenseCollection().getCircleTransactions());

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
                        getExpenseCollection().cancelExpense(expense);
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
                btnRemind.setVisibility(View.VISIBLE);
                btnRemind.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendReminder(context, expense);
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
                        getExpenseCollection().changeTransactionStatus(context, expense, !card.isChecked(), card);
                        return true;
                    }
                });

                // determine if card should be marked completed
                Transaction transaction = getMyExpenseTransaction(expense, getExpenseCollection().getCircleTransactions());
                if(transaction != null){
                    card.setChecked(transaction.getCompleted());
                }
                else{
                    return;
                }

                // set card body
                String payTo = "Pay to " + receiver.getString("name");
                tvPayTo.setText(payTo);
                String oweAmount = "You owe: $" + String.format("%.2f", transaction.getAmount());
                tvTotal.setText(oweAmount);

                // set custom visibilities
                tvTotal.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.GONE);
                btnRemind.setVisibility(View.GONE);
                btnMarkPaid.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
                btnMarkPaid.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getExpenseCollection().changeTransactionStatus(context, expense, !card.isChecked(), card);
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


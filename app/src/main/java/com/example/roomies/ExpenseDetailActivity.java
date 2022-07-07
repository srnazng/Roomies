package com.example.roomies;

import static com.example.roomies.ExpenseFragment.getFilterInt;
import static com.example.roomies.ExpenseFragment.updateExpenseList;
import static com.example.roomies.utils.ExpenseUtils.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomies.model.Expense;
import com.example.roomies.model.Transaction;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.parse.ParseUser;

import java.util.List;

public class ExpenseDetailActivity extends AppCompatActivity {
    private Expense expense;
    private List<Transaction> transactions;
    private Transaction myTransaction;

    private TextView tvExpenseReason;
    private TextView tvCreator;
    private TextView tvAmount;
    private ChipGroup assigneeChipGroup;
    private com.google.android.material.card.MaterialCardView expenseDetailCard;

    private Button btnDetailMarkPaid;
    private Button btnDetailEdit;
    private Button btnDetailCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);
        expense = getIntent().getParcelableExtra("expense");

        // bind with views
        btnDetailCancel = findViewById(R.id.btnDetailCancel);
        btnDetailMarkPaid = findViewById(R.id.btnDetailMarkPaid);
        btnDetailEdit = findViewById(R.id.btnDetailEditExpense);
        expenseDetailCard = findViewById(R.id.expenseDetailCard);
        tvExpenseReason = findViewById(R.id.tvExpenseReason);
        tvCreator = findViewById(R.id.tvCreator);
        tvAmount = findViewById(R.id.tvAmount);
        assigneeChipGroup = findViewById(R.id.assigneeChipGroup);

        // set expense title
        tvExpenseReason.setText(expense.getName() + " $" + String.format("%.2f", expense.getTotal()));

        // get all transactions relating to expense
        transactions = getAllExpenseTransactions(expense, getCircleTransactions());

        // show all users assigned expense
        initializeChips();

        // view if user is assigned to pay expense
        if(isPayment()){
            tvCreator.setText("Pay to " + expense.getCreator().getString("name"));
            tvAmount.setText("You owe: $" + String.format("%.2f", myTransaction.getAmount()));
            tvAmount.setVisibility(View.VISIBLE);

            expenseDetailCard.setCheckable(true);
            expenseDetailCard.setLongClickable(true);
            expenseDetailCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    changeTransactionStatus(expense, !expenseDetailCard.isChecked(), expenseDetailCard);
                    return true;
                }
            });

            expenseDetailCard.setChecked(myTransaction.getCompleted());

            btnDetailMarkPaid.setVisibility(View.VISIBLE);
            btnDetailEdit.setVisibility(View.GONE);
            btnDetailCancel.setVisibility(View.GONE);

            btnDetailMarkPaid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeTransactionStatus(expense, !expenseDetailCard.isChecked(), expenseDetailCard);
                }
            });
        }
        // view if user created expense
        else if(isRequest()){
            tvCreator.setText("Created by " + expense.getCreator().getString("name"));
            tvAmount.setVisibility(View.GONE);

            // determine if card should be marked completed
            expenseDetailCard.setChecked(true);
            for(int i=0; i<transactions.size(); i++){
                if(!transactions.get(i).getCompleted()){
                    expenseDetailCard.setChecked(false);
                    break;
                }
            }

            btnDetailMarkPaid.setVisibility(View.GONE);
            btnDetailEdit.setVisibility(View.VISIBLE);
            btnDetailCancel.setVisibility(View.VISIBLE);

            btnDetailCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ExpenseDetailActivity.this, "Cancel expense success", Toast.LENGTH_SHORT).show();
                    cancelExpense(expense);
                    updateExpenseList(getFilterInt());
                    finish();
                }
            });
        }
        else{
            tvCreator.setText("Created by " + expense.getCreator().getString("name"));
            tvAmount.setVisibility(View.GONE);

            // card settings
            expenseDetailCard.setChecked(true);
            expenseDetailCard.setLongClickable(false);

            // determine if card should be marked completed
            for(int i=0; i<transactions.size(); i++){
                if(!transactions.get(i).getCompleted()){
                    expenseDetailCard.setChecked(false);
                    break;
                }
            }

            btnDetailMarkPaid.setVisibility(View.GONE);
            btnDetailEdit.setVisibility(View.GONE);
            btnDetailCancel.setVisibility(View.GONE);
        }
    }

    // determine if current user is assigned to pay for expense
    private boolean isPayment(){
        if(transactions == null || transactions.isEmpty()){
            transactions = getAllExpenseTransactions(expense, getCircleTransactions());
        }

        for(int i = 0; i<transactions.size(); i++){
            if(transactions.get(i).getPayer().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
                myTransaction = transactions.get(i);
                return true;
            }
        }

        return false;
    }

    // determine if current user created expense
    private boolean isRequest(){
        if(!transactions.isEmpty() && transactions.get(0).getReceiver().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
            return true;
        }

        return false;
    }

    // set chips to all users in list of transactions
    public void initializeChips(){
        if(transactions == null || transactions.isEmpty()){
            transactions = getAllExpenseTransactions(expense, getCircleTransactions());
        }

        if(assigneeChipGroup == null){
            return;
        }

        // clear ChipGroup
        assigneeChipGroup.removeAllViews();

        // create and add new chips
        for(int i=0; i<transactions.size(); i++){
            Chip chip = new Chip(this);
            Transaction t = transactions.get(i);
            chip.setText(t.getPayer().getString("name"));
            chip.setClickable(false);
            chip.setCheckable(true);
            chip.setChecked(t.getCompleted());
            assigneeChipGroup.addView(chip);
        }
    }
}
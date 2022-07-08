package com.example.roomies;

import static com.example.roomies.ExpenseFragment.getFilterInt;
import static com.example.roomies.ExpenseFragment.updateExpenseList;
import static com.example.roomies.utils.ExpenseUtils.*;
import static com.example.roomies.utils.Utils.showImage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.roomies.adapter.ExpenseCommentsAdapter;
import com.example.roomies.model.Expense;
import com.example.roomies.model.ExpenseComment;
import com.example.roomies.model.Transaction;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ExpenseDetailActivity extends AppCompatActivity {
    private Expense expense;
    private List<Transaction> transactions;
    private List<ExpenseComment> comments;
    private ExpenseCommentsAdapter adapter;
    private Transaction myTransaction;
    private ImageView ivProfile;
    private ImageView ivSend;
    private ImageView ivProof;
    private EditText etComment;

    private RecyclerView rvComments;
    private TextView tvExpenseReason;
    private TextView tvCreator;
    private TextView tvAmount;
    private ChipGroup assigneeChipGroup;
    private com.google.android.material.card.MaterialCardView expenseDetailCard;

    private Button btnDetailMarkPaid;
    private Button btnDetailEdit;
    private Button btnDetailCancel;
    private ImageButton btnVenmo;
    private ImageButton btnCashApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);
        expense = getIntent().getParcelableExtra("expense");

        // bind with views
        btnDetailCancel = findViewById(R.id.btnDetailCancel);
        btnDetailMarkPaid = findViewById(R.id.btnDetailMarkPaid);
        btnDetailEdit = findViewById(R.id.btnDetailEditExpense);
        btnVenmo = findViewById(R.id.btnVenmo);
        btnVenmo.setVisibility(View.GONE);
        btnCashApp = findViewById(R.id.btnCashApp);
        btnCashApp.setVisibility(View.GONE);
        expenseDetailCard = findViewById(R.id.expenseDetailCard);
        tvExpenseReason = findViewById(R.id.tvExpenseReason);
        tvCreator = findViewById(R.id.tvCreator);
        tvAmount = findViewById(R.id.tvAmount);
        assigneeChipGroup = findViewById(R.id.assigneeChipGroup);
        rvComments = findViewById(R.id.rvComments);
        ivProof = findViewById(R.id.ivProof);

        ivProof.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImage(ExpenseDetailActivity.this, Uri.parse(expense.getProof().getUrl()));
            }
        });

        // set up comments Recycler View
        comments = new ArrayList<>();
        adapter = new ExpenseCommentsAdapter(this, comments);
        rvComments.setAdapter(adapter);
        rvComments.setLayoutManager(new LinearLayoutManager(this));

        // let user send comment under expense
        ivProfile = findViewById(R.id.ivProfile);
        ParseFile image = ParseUser.getCurrentUser().getParseFile("image");
        if (image != null) {
            Glide.with(this).load(image.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivProfile);
        }
        etComment = findViewById(R.id.etComment);
        ivSend = findViewById(R.id.ivSend);
        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etComment.getText().toString().isEmpty()){
                    return;
                }
                sendComment(expense, etComment.getText().toString(), comments, adapter);
                etComment.setText("");
            }
        });

        initDetails();

        // view if user is assigned to pay expense
        if(isPayment()){
            setPaymentView();
        }
        // view if user created expense
        else if(isRequest()){
            setRequestView();
        }
        else{
            setDefaultView();
        }
    }

    public void initDetails(){
        initComments(expense, comments, adapter);
        // set expense title
        tvExpenseReason.setText(expense.getName() + " $" + String.format("%.2f", expense.getTotal()));
        // get all transactions relating to expense
        transactions = getAllExpenseTransactions(expense, getCircleTransactions());
        // show all users assigned expense
        initializeChips();

        // show receipt
        if(expense.getProof() != null){
            Glide.with(this).load(expense.getProof().getUrl()).into(ivProof);
        }
        else{
            ivProof.setVisibility(View.GONE);
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

    private void setPaymentView(){
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

        String venmo = myTransaction.getReceiver().getString("venmo");
        if(venmo != null && !venmo.isEmpty()){
            btnVenmo.setVisibility(View.VISIBLE);
            btnVenmo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("https://account.venmo.com/u/" + venmo);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        }

        String cashApp = myTransaction.getReceiver().getString("cashApp");
        if(cashApp != null && !cashApp.isEmpty()){
            btnCashApp.setVisibility(View.VISIBLE);
            btnCashApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("https://cash.app/" + cashApp);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        }
    }

    private void setRequestView(){
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
        btnDetailEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ExpenseDetailActivity.this, EditExpenseActivity.class);
                i.putExtra("expense", expense);
                i.putExtra("fromDetails", true);
                startActivity(i);
            }
        });
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

    private void setDefaultView(){
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
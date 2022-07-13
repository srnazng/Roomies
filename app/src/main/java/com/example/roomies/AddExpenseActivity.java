package com.example.roomies;
import static com.example.roomies.utils.ExpenseUtils.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomies.model.UserCircle;
import com.example.roomies.utils.CircleUtils;
import com.example.roomies.utils.NumberTextWatcher;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class AddExpenseActivity extends AppCompatActivity {
    private EditText etExpenseName;
    private EditText etTotal;
    private TextView tvAssignTo;
    private CheckBox checkSplit;
    private Button btnSubmitExpense;
    private LinearLayout transactions;
    private TextView tvSplitSum;
    private ConstraintLayout layoutAssign;
    private Button btnSplit;
    private Button btnSelectAll;

    private List<View> transactionViews;
    private List<ParseUser> transactionUsers;

    public static final String TAG = "AddExpenseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // list of views that show up for each user that can be assigned to pay for expense
        transactionViews = new ArrayList<>();
        // list of users that can be assigned to pay for expense
        transactionUsers = new ArrayList<>();

        // only show section to assign users to pay for expense if split is checked
        tvAssignTo = findViewById(R.id.tvAssignTo);
        tvAssignTo.setVisibility(View.VISIBLE);
        transactions = findViewById(R.id.transactions);
        transactions.setVisibility(View.VISIBLE);
        fillUserTransactions();

        // name of expense
        etExpenseName = findViewById(R.id.etExpenseName);

        // total expense amount
        etTotal = findViewById(R.id.etTotal);
        etTotal.addTextChangedListener(new NumberTextWatcher(etTotal));

        // button to submit expense to database
        btnSubmitExpense = findViewById(R.id.btnSubmitExpense);
        btnSubmitExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitExpense(AddExpenseActivity.this, etExpenseName, etTotal,
                        checkSplit.isChecked(), transactionViews, transactionUsers);
            }
        });

        // checkbox determining if expense is divided among users
        checkSplit = findViewById(R.id.checkSplit);
        layoutAssign = findViewById(R.id.layoutAssign);
        checkSplit.setChecked(true);
        checkSplit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            // toggle showing the assign to section
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checkSplit.isChecked()){
                    layoutAssign.setVisibility(View.VISIBLE);
                }
                else{
                    layoutAssign.setVisibility(View.GONE);
                }
            }
        });

        // split cost evenly
        btnSplit = findViewById(R.id.btnSplit);
        btnSplit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                splitCost(AddExpenseActivity.this,
                        etTotal.getText().toString(),
                        transactionViews,
                        tvSplitSum,
                        getNumAssigned(transactionViews));
            }
        });

        // select all users in circle
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAll();
            }
        });
    }

    // add views showing users that can be assigned to pay for expense
    public void fillUserTransactions(){
        List<UserCircle> userCircleList = CircleUtils.getUserCircleList();
        if(userCircleList == null){
            Toast.makeText(this, "Error accessing circle", Toast.LENGTH_SHORT).show();
        }
        for(int i=0; i<userCircleList.size(); i++){
            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.transaction_user, null);

            transactionViews.add(v);
            transactionUsers.add(userCircleList.get(i).getUser());

            EditText etAmount = v.findViewById(R.id.etAmount);
            etAmount.setVisibility(View.INVISIBLE);
            etAmount.addTextChangedListener(new NumberTextWatcher(etAmount){
                @Override
                public void afterTextChanged(Editable s) {
                    super.afterTextChanged(s);
                    // update sum of assigned transactions
                    findAssignedSum(tvSplitSum, transactionViews);
                }
            });

            CheckBox checkName = v.findViewById(R.id.checkName);
            checkName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

                // only show input of price user is paying if they are assigned
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        etAmount.setVisibility(View.VISIBLE);
                    }
                    else{
                        etAmount.setVisibility(View.INVISIBLE);
                    }
                }
            });

            // name of user
            checkName.setText(userCircleList.get(i).getUser().getString("name"));

            // add to view of all transactions
            transactions.addView(v);
        }

        // TextView showing sum of assigned transactions
        tvSplitSum = new TextView(this);
        findAssignedSum(tvSplitSum, transactionViews);
        tvSplitSum.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        transactions.addView(tvSplitSum);
    }

    // select all users in circle to pay expense
    public void selectAll(){
        for(int i=0; i<transactionViews.size(); i++){
            View view = transactionViews.get(i);
            CheckBox checkName = view.findViewById(R.id.checkName);
            checkName.setChecked(true);
        }
    }
}
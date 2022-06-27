package com.example.roomies;
import static com.example.roomies.HomeFragment.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomies.R;
import com.example.roomies.model.Expense;
import com.example.roomies.model.Transaction;
import com.google.android.material.chip.ChipGroup;
import com.parse.ParseFile;
import com.parse.ParseObject;
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
    private Expense expense;

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
        etTotal.setText("$ ");

        // button to submit expense to database
        btnSubmitExpense = findViewById(R.id.btnSubmitExpense);
        btnSubmitExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitExpense();
            }
        });

        // checkbox determining if expense is divided among users
        checkSplit = findViewById(R.id.checkSplit);
        checkSplit.setChecked(true);
        checkSplit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            // toggle showing the assign to section
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checkSplit.isChecked()){
                    transactions.setVisibility(View.VISIBLE);
                    tvAssignTo.setVisibility(View.VISIBLE);
                }
                else{
                    transactions.setVisibility(View.GONE);
                    tvAssignTo.setVisibility(View.GONE);
                }
            }
        });
    }

    // add views showing users that can be assigned to pay for expense
    public void fillUserTransactions(){
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

            TextView tvDollar = v.findViewById(R.id.tvDollar);
            tvDollar.setVisibility(View.INVISIBLE);

            CheckBox checkName = v.findViewById(R.id.checkName);
            checkName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

                // only show input of price user is paying if they are assigned
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        etAmount.setVisibility(View.VISIBLE);
                        tvDollar.setVisibility(View.VISIBLE);
                    }
                    else{
                        etAmount.setVisibility(View.INVISIBLE);
                        tvDollar.setVisibility(View.INVISIBLE);
                    }
                }
            });

            // name of user
            checkName.setText(userCircleList.get(i).getUser().getString("name"));

            // add to view of all transactions
            transactions.addView(v);
        }
    }

    // add Expense object on database
    public void submitExpense(){
       Expense entity = new Expense();
        entity.put("name", etExpenseName.getText().toString());

        String price = etTotal.getText().toString();
        if(!price.isEmpty() && price.charAt(0) == '$'){
            price = price.substring(1);
        }
        entity.put("total", Float.parseFloat(price));
        entity.put("creator", ParseUser.getCurrentUser());

        // TODO: upload photo proof
        // entity.put("proof", new ParseFile("resume.txt", "My string content".getBytes()));

        expense = entity;

        // Saves the new object.
        entity.saveInBackground(e -> {
            if (e==null){
                //Save was done
                if(checkSplit.isChecked()){
                    // assign expense to specific users
                    submitTransactions();
                }
                else{
                    // done
                    Toast.makeText(this, "Added expense", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }else{
                //Something went wrong
                Toast.makeText(this, "Error adding expense", Toast.LENGTH_LONG).show();
                Log.e(TAG, e.getMessage());
            }
        });
    }

    // add Transaction objects for each user assigned to pay expense
    public void submitTransactions(){
        String currentUser = ParseUser.getCurrentUser().getObjectId();

        // loop through all users in circle
        for(int i=0; i<transactionViews.size(); i++){
            View view = transactionViews.get(i);
            CheckBox checkName = view.findViewById(R.id.checkName);

            // user is assigned to help pay for this expense and is not current user
            if(checkName.isChecked() && !transactionUsers.get(i).getObjectId().equals(currentUser)){
                EditText etAmount = view.findViewById(R.id.etAmount);
                Transaction entity = new Transaction();

                // current user
                entity.put("receiver", ParseUser.getCurrentUser());
                // assigned user
                entity.put("payer", transactionUsers.get(i));
                // amount specified
                if(!etAmount.getText().toString().isEmpty()){
                    entity.put("amount", Float.parseFloat(etAmount.getText().toString()));
                }
                // related Expense object
                entity.put("expense", expense);
                // transaction status
                entity.put("completed", false);

                // Saves the new object.
                // Notice that the SaveCallback is totally optional!
                entity.saveInBackground(e -> {
                    if (e==null){
                        //Save was done
                    }else{
                        //Something went wrong
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(this, "Error making transaction", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        }

        // all transactions successfully saved
        Toast.makeText(this, "Added expense", Toast.LENGTH_SHORT).show();
        finish();
    }
}
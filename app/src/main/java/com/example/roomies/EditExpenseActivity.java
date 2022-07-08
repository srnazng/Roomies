package com.example.roomies;

import static com.example.roomies.utils.ExpenseUtils.editExpense;
import static com.example.roomies.utils.ExpenseUtils.removeDollar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.roomies.model.Expense;
import com.example.roomies.utils.NumberTextWatcher;

public class EditExpenseActivity extends AppCompatActivity {
    private EditText etEditExpenseName;
    private EditText etEditTotal;
    private Button btnUpdateExpense;

    private Expense expense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        // get expense
        expense = getIntent().getParcelableExtra("expense");

        // fill in existing expense details
        etEditExpenseName = findViewById(R.id.etEditExpenseName);
        etEditExpenseName.setText(expense.getName());
        etEditTotal = findViewById(R.id.etEditTotal);
        etEditTotal.addTextChangedListener(new NumberTextWatcher(etEditTotal));
        etEditTotal.setText("$" + String.format("%.2f", expense.getTotal()));

        // update expense
        btnUpdateExpense = findViewById(R.id.btnUpdateExpense);
        btnUpdateExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editExpense(expense,
                        etEditExpenseName.getText().toString(),
                        Float.parseFloat(removeDollar(etEditTotal.getText().toString())));
                finish();
            }
        });
    }
}
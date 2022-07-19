package com.example.roomies;

import static com.example.roomies.utils.ExpenseUtils.editExpense;
import static com.example.roomies.utils.ExpenseUtils.removeDollar;
import static com.example.roomies.utils.Utils.GET_FROM_GALLERY;
import static com.example.roomies.utils.Utils.getPath;
import static com.example.roomies.utils.Utils.showImage;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.roomies.model.Expense;
import com.example.roomies.utils.NumberTextWatcher;

import java.io.FileNotFoundException;
import java.io.IOException;

public class EditExpenseActivity extends AppCompatActivity {
    private EditText etEditExpenseName;
    private EditText etEditTotal;
    private Button btnUpdateExpense;
    private Button btnEditUpload;
    private TextView tvEditedFileName;
    private TextView tvViewEditedImage;
    private TextView tvDeleteEdited;

    private Expense expense;
    private Bitmap bitmap;
    private Uri uri;

    private boolean fromDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        // get expense
        expense = getIntent().getParcelableExtra("expense");
        fromDetails = getIntent().getBooleanExtra("fromDetails", false);

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
                editExpense(EditExpenseActivity.this,
                        expense,
                        etEditExpenseName.getText().toString(),
                        Float.parseFloat(removeDollar(etEditTotal.getText().toString())),
                        bitmap);
                if(fromDetails){
                    Intent i = new Intent(EditExpenseActivity.this, ExpenseDetailActivity.class);
                    i.putExtra("expense", expense);
                    startActivity(i);
                }
                else {
                    finish();
                }
            }
        });

        // upload image
        btnEditUpload = findViewById(R.id.btnEditUpload);
        tvEditedFileName = findViewById(R.id.tvEditedFileName);
        tvViewEditedImage = findViewById(R.id.tvViewEditedImage);
        tvDeleteEdited = findViewById(R.id.tvDeleteEdited);

        tvViewEditedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImage(EditExpenseActivity.this, uri);
            }
        });

        tvDeleteEdited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = null;
                uri = null;
                tvEditedFileName.setVisibility(View.INVISIBLE);
                tvViewEditedImage.setVisibility(View.INVISIBLE);
                tvDeleteEdited.setVisibility(View.INVISIBLE);
            }
        });

        btnEditUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        if(expense.getProof() == null){
            tvEditedFileName.setVisibility(View.INVISIBLE);
            tvViewEditedImage.setVisibility(View.INVISIBLE);
            tvDeleteEdited.setVisibility(View.INVISIBLE);
        }
        else{
            uri = Uri.parse(expense.getProof().getUrl());
            tvEditedFileName.setVisibility(View.VISIBLE);
            tvEditedFileName.setText(expense.getProof().getName());
            tvViewEditedImage.setVisibility(View.VISIBLE);
            tvDeleteEdited.setVisibility(View.VISIBLE);
        }
    }

    // after upload new image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                // set bitmap to uploaded image
                bitmap = MediaStore.Images.Media.getBitmap(EditExpenseActivity.this.getContentResolver(), selectedImage);
                uri = selectedImage;
                tvEditedFileName.setText(getPath(EditExpenseActivity.this, selectedImage));
                tvEditedFileName.setVisibility(View.VISIBLE);
                tvViewEditedImage.setVisibility(View.VISIBLE);
                tvDeleteEdited.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
package com.example.roomies;

import static com.example.roomies.utils.UserUtils.createAccount;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignUpActivity extends AppCompatActivity {
    private Button btnCreateAccount;
    private TextView tvBack;
    private EditText etNameInput;
    private EditText etEmailInput;
    private EditText etPasswordInput;
    private EditText etReenterPasswordInput;

    public static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etNameInput = findViewById(R.id.etNameInput);
        etEmailInput = findViewById(R.id.etEmailInput);
        etPasswordInput = findViewById(R.id.etPasswordInput);
        etReenterPasswordInput = findViewById(R.id.etReenterPasswordInput);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(SignUpActivity.this,
                        etNameInput.getText().toString(),
                        etEmailInput.getText().toString(),
                        etPasswordInput.getText().toString(),
                        etReenterPasswordInput.getText().toString());
            }
        });
        tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
               startActivity(i);
            }
        });
    }
}


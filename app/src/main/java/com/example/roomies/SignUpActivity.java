package com.example.roomies;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

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
        etEmailInput = findViewById(R.id.etChoreName);
        etPasswordInput = findViewById(R.id.etPasswordInput);
        etReenterPasswordInput = findViewById(R.id.etReenterPasswordInput);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
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
    protected void createAccount(){
        if(!etPasswordInput.getText().toString().equals(etReenterPasswordInput.getText().toString())){
            Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(etEmailInput.getText().toString());
        user.setPassword(etPasswordInput.getText().toString());
        user.setEmail(etEmailInput.getText().toString());
        user.put("name", etNameInput.getText().toString());

        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Intent i = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Log.e(TAG, "sign up failed " + e);
                }
            }
        });
    }
}


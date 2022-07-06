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

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";

    private TextView tvRegister;
    private Button btnLogin;
    private EditText etEmailInput;
    private EditText etPasswordInput;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(ParseUser.getCurrentUser() != null){
            goMainActivity();
        }

        etEmailInput = findViewById(R.id.etEmailInput);
        etPasswordInput = findViewById(R.id.etPasswordInput);

        tvRegister = findViewById(R.id.tvRegister);
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });
        btnLogin = findViewById(R.id.btnCreateAccount);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick login button");
                String username = etEmailInput.getText().toString();
                String password = etPasswordInput.getText().toString();
                loginUser(username, password);
            }
        });
    }


    private void loginUser(String username, String password){
        Log.i(TAG, "attempt login");

        // login in background thread
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e != null){
                    // issue
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(LoginActivity.this, "Incorrect login credentials", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e(TAG, "login success");
                goMainActivity();
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
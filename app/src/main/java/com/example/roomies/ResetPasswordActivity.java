package com.example.roomies;

import static com.example.roomies.utils.UserUtils.passwordReset;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText etMyEmail;
    private Button btnEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etMyEmail = findViewById(R.id.etMyEmail);
        btnEmail = findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordReset(ResetPasswordActivity.this, etMyEmail.getText().toString());
                etMyEmail.setText("");
            }
        });
    }
}
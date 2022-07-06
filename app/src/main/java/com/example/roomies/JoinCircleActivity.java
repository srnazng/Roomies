package com.example.roomies;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomies.model.Circle;
import com.example.roomies.model.UserCircle;
import com.example.roomies.utils.SessionUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

// Allows user to join an existing circle using a join code
public class JoinCircleActivity extends AppCompatActivity {
    private Button btnJoin;
    private TextView tvLogin;
    private EditText etCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_circle);

        // text field to enter join code
        etCode = findViewById(R.id.etCode);

        // join circle button
        btnJoin = findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinCircle();
            }
        });

        // back button
        tvLogin = findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(JoinCircleActivity.this, AddCircleActivity.class);
                startActivity(i);
            }
        });
    }

    // join existing circle using join code
    private void joinCircle(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Circle");

        // The query will search for a ParseObject, given its objectId.
        // When the query finishes running, it will invoke the GetCallback
        // with either the object, or the exception thrown
        query.getInBackground(etCode.getText().toString(), (object, e) -> {
            if (e == null) {
                Circle circle = (Circle) object;
                // Create new UserCircle
                UserCircle userCircle = new UserCircle();

                userCircle.put("user", ParseUser.getCurrentUser());
                userCircle.put("circle", circle);

                // Saves the new object.
                userCircle.saveInBackground(e1 -> {
                    if (e1==null){
                        Toast.makeText(JoinCircleActivity.this, "Circle join success", Toast.LENGTH_SHORT).show();
                        SessionUtils.startSession(JoinCircleActivity.this);
                    }else{
                        //Something went wrong
                        Toast.makeText(JoinCircleActivity.this, "Error occurred: unable to join circle", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // something went wrong
                Toast.makeText(JoinCircleActivity.this, "Invalid join code", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
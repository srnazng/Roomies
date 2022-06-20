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
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;
import org.w3c.dom.Text;

public class JoinCircleActivity extends AppCompatActivity {
    private Button btnJoin;
    private TextView tvLogin;
    private EditText etCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_circle);

        etCode = findViewById(R.id.etCode);

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
                Intent i = new Intent(JoinCircleActivity.this, CircleActivity.class);
                startActivity(i);
            }
        });
    }

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
                // Notice that the SaveCallback is totally optional!
                userCircle.saveInBackground(e1 -> {
                    if (e1==null){
                        Intent i = new Intent(JoinCircleActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }else{
                        //Something went wrong
                        Toast.makeText(JoinCircleActivity.this, e1.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // something went wrong
                Toast.makeText(JoinCircleActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
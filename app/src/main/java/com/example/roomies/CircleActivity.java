package com.example.roomies;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.roomies.model.Circle;
import com.example.roomies.model.UserCircle;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class CircleActivity extends AppCompatActivity {
    private Button btnCreateCircle;
    private Button btnJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle);

        btnCreateCircle = findViewById(R.id.btnCreateCircle);
        btnCreateCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCircle();
            }
        });

        btnJoin = findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CircleActivity.this, JoinCircleActivity.class);
                startActivity(i);
            }
        });
    }

    public void createCircle(){
        // Create new circle
        Circle circle = new Circle();

        // Saves the new object.
        // Notice that the SaveCallback is totally optional!
        circle.saveInBackground(e -> {
            if (e==null){
                // Create new UserCircle
                UserCircle userCircle = new UserCircle();

                userCircle.put("user", ParseUser.getCurrentUser());
                userCircle.put("circle", circle);

                // Saves the new object.
                // Notice that the SaveCallback is totally optional!
                userCircle.saveInBackground(e1 -> {
                    if (e1==null){
                        Intent i = new Intent(CircleActivity.this, NewCircleActivity.class);
                        i.putExtra("circle", Parcels.wrap(circle));
                        startActivity(i);
                        finish();
                    }else{
                        //Something went wrong
                        Toast.makeText(this, e1.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                //Something went wrong
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.example.roomies;

import static com.example.roomies.utils.UserUtils.logout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomies.model.Circle;
import com.example.roomies.model.UserCircle;
import com.parse.ParseUser;

import org.parceler.Parcels;

// Presents user the options of creating a new circle or joining an existing circle
public class AddCircleActivity extends AppCompatActivity {
    private Button btnCreateCircle;
    private Button btnJoin;
    private TextView tvLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_circle);

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
                Intent i = new Intent(AddCircleActivity.this, JoinCircleActivity.class);
                startActivity(i);
            }
        });

        tvLogOut = findViewById(R.id.tvLogOut);
        tvLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(AddCircleActivity.this);
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
                        Toast.makeText(this, "Create new circle success", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(AddCircleActivity.this, NewCircleActivity.class);
                        i.putExtra("circle", Parcels.wrap(circle));
                        startActivity(i);
                        finish();
                    }else{
                        //Something went wrong
                        Toast.makeText(this, "Error occurred unable to create new circle", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                //Something went wrong
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
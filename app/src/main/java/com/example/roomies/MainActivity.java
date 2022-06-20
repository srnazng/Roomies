package com.example.roomies;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.roomies.model.Circle;
import com.example.roomies.model.UserCircle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final FragmentManager fragmentManager = getSupportFragmentManager();

    // define fragments
    final Fragment homeFragment = HomeFragment.newInstance();
    final Fragment choreFragment = ChoreFragment.newInstance();
    final Fragment expenseFragment = ExpenseFragment.newInstance();
    final Fragment settingsFragment = SettingsFragment.newInstance();

    List<Circle> circles;

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circles = new ArrayList<>();
        getCircles();

        // set up bottom navigator
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            Fragment selectedFragment = homeFragment;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_chores:
                        selectedFragment = choreFragment;
                        break;
                    case R.id.action_expenses:
                        selectedFragment = expenseFragment;
                        break;
                    case R.id.action_settings:
                        selectedFragment = settingsFragment;
                        break;
                    default: selectedFragment = homeFragment;
                }
                fragmentManager.beginTransaction().replace(R.id.frame, selectedFragment).commit();
                return true;
            }
        });
    }

    public void getCircles(){
        // specify what type of data we want to query - Like.class
        ParseQuery<UserCircle> query = ParseQuery.getQuery(UserCircle.class).whereEqualTo(UserCircle.KEY_USER, ParseUser.getCurrentUser());
        // include data referred by user key
        query.include(UserCircle.KEY_USER);

        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting userCircles", e);
                    return;
                }

                if(userCircles.isEmpty()){
                    Intent i = new Intent(MainActivity.this, CircleActivity.class);
                    startActivity(i);
                    finish();
                }

                // save received posts to list and notify adapter of new data
                circles.clear();
                for(UserCircle userCircle : userCircles){
                    circles.add(userCircle.getCircle());
                }
            }
        });
    }

}
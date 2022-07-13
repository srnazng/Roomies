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
import com.example.roomies.utils.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final FragmentManager fragmentManager = getSupportFragmentManager();

    private List<Circle> circles;
    private Session session;

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new Session(MainActivity.this);

        // all circles which user has joined
        // currently user can only join 1 circle
        circles = new ArrayList<>();
        updateCircles();

        // set up bottom navigator
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            // initialize fragment
            Fragment selectedFragment = null;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_chores:
                        // go to chore screen
                        selectedFragment = ChoreFragment.newInstance();
                        break;
                    case R.id.action_expenses:
                        // go to house expense screen
                        selectedFragment = ExpenseFragment.newInstance();
                        break;
                    case R.id.action_settings:
                        // refresh circle info
                        updateCircles();
                        // go to settings screen (manage user account and circle)
                        selectedFragment = SettingsFragment.newInstance();
                        break;
                    default:
                        // go to home screen
                        selectedFragment = HomeFragment.newInstance();
                }
                fragmentManager.beginTransaction().replace(R.id.frame, selectedFragment).commit();
                return true;
            }
        });
    }

    /**
     * query UserCircle objects that contain current user to get circles that user has joined
     * TODO: return circle, add to utils
     */
    public void updateCircles(){
        // specify what type of data we want to query - UserCircle.class
        ParseQuery<UserCircle> query = ParseQuery.getQuery(UserCircle.class).whereEqualTo(UserCircle.KEY_USER, ParseUser.getCurrentUser());
        // include data referred by user key
        query.include(UserCircle.KEY_USER);
        query.include(UserCircle.KEY_CIRCLE);
        // start an asynchronous call for UserCircle objects that include current user
        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting userCircles", e);
                    return;
                }

                // user has not joined a circle
                if(userCircles.isEmpty()){
                    // go to AddCircleActivity
                    Intent i = new Intent(MainActivity.this, AddCircleActivity.class);
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
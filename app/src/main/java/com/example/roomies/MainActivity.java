package com.example.roomies;

import static com.example.roomies.utils.CircleUtils.getCurrentCircle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.roomies.model.Circle;
import com.example.roomies.utils.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
        circles.add(getCurrentCircle());

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
}
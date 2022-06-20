package com.example.roomies;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    final FragmentManager fragmentManager = getSupportFragmentManager();

    // define fragments
    final Fragment homeFragment = HomeFragment.newInstance();
    final Fragment choreFragment = ChoreFragment.newInstance();
    final Fragment expenseFragment = ExpenseFragment.newInstance();
    final Fragment settingsFragment = SettingsFragment.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


}
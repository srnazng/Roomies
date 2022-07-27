package com.example.roomies;

import static com.example.roomies.model.CircleManager.getUserCircleList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.roomies.adapter.ProfileAdapter;
import com.example.roomies.model.UserCircle;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class CircleProfilesActivity extends AppCompatActivity {
    RecyclerView rvProfiles;
    ProfileAdapter adapter;
    List<ParseUser> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_profiles);

        // Lookup the recyclerview in activity layout
        rvProfiles = (RecyclerView) findViewById(R.id.rvProfiles);

        // Initialize users
        users = getUsers();

        // Create adapter
        adapter = new ProfileAdapter(CircleProfilesActivity.this, users);
        // Attach the adapter to the recyclerview to populate items
        rvProfiles.setAdapter(adapter);
        // Set layout manager to position the items
        rvProfiles.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onResume() {
        super.onResume();
        users = getUsers();
        adapter.notifyDataSetChanged();
    }

    // get all users in circle
    public List<ParseUser> getUsers(){
        List<ParseUser> list = new ArrayList<>();

        List<UserCircle> userCircles = getUserCircleList();
        for(int i=0; i<userCircles.size(); i++){
            list.add(userCircles.get(i).getUser());
        }

        return list;
    }
}